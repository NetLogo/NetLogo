// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mirror

import org.nlogo.api.{ Link, Patch, Turtle, World, WorldPropertiesInterface }
import collection.JavaConverters._

/**
 * This class implements a server-side cache of view mirroring-related world data. view updates are
 * incremental whenever possible, so we keep track here of the state of the view as of the last
 * client update. To do a new update, we compare this with the actual state of the world, record
 * updates here and compile a list of differences to send to clients. See updateWorld() below.
 * 
 * This class also supports serialization directly to a byte array, which is used in those cases
 * when we need a full (i.e. non-incremental) view update.  Newly entered clients, for instance,
 * will therefore be send a copy of the view data as it was last sent to the other clients. See
 * toByteArray below.
 *
 * In order to be thread safe, the policy for this class is that all methods must be called from the
 * event thread.  Further, for some of the methods, we require that it is safe for us to lock on the
 * world.  In particular, any code that accesses the current state of the world must have a lock on
 * the world.  All such methods are commented.
 */
class ServerWorld(settings: WorldPropertiesInterface) {

  private var turtles = collection.mutable.HashMap[Long, TurtleData]()
  private val patches = collection.mutable.HashMap[Long, PatchData ]()
  private var links   = collection.mutable.HashMap[ClientWorld.LinkKey, LinkData]()

  private var minPxcor, minPycor, maxPxcor, maxPycor = 0
  private var fontSize = 0
  private var xWrap, yWrap = false
  private var perspective = new AgentPerspective(null, 0, 0, -1, true)
  private var drawing: java.awt.image.BufferedImage = null

  /**
   * updates all local world data to match world, constructing diffs in the process. This is
   * synchronized because it inspects, modifies and may overwrite patches and turtles. This should
   * be fine since it should only ever be called from a single thread.
   * 
   * This method MUST be called from the event thread, and it must be safe for us to get a lock on
   * the world.
   *
   * @return a new DiffBuffer containing the differences between local world data and that in world.
   */
  def updateWorld(world: World, resetWorld: Boolean): DiffBuffer =
    synchronized {
      val buf = new DiffBuffer
      world.synchronized {
        updateGeneral(world, buf)
        updatePatches(world, buf)
        updateTurtles(world, buf)
        updateLinks(world, buf)
        updateDrawing(world, buf, resetWorld)
      }
      buf
    }

  /**
   * updates local general view data (sex/y, label font size, etc.) to match the current state of
   * the world, storing diffs in buf.
   */
  private def updateGeneral(world: World, buf: DiffBuffer) {
    if (minPxcor != world.minPxcor) {
      minPxcor = world.minPxcor
      buf.addMinX(minPxcor)
    }
    if (minPycor != world.minPycor) {
      minPycor = world.minPycor
      buf.addMinY(minPycor)
    }
    if (maxPxcor != world.maxPxcor) {
      maxPxcor = world.maxPxcor
      buf.addMaxX(maxPxcor)
    }
    if (maxPycor != world.maxPycor) {
      maxPycor = world.maxPycor
      buf.addMaxY(maxPycor)
    }
    if (fontSize != settings.fontSize) {
      fontSize = settings.fontSize
      buf.addFontSize(fontSize)
    }
    if (xWrap != world.wrappingAllowedInX) {
      xWrap = world.wrappingAllowedInX
      buf.addWrapX(xWrap)
    }
    if (yWrap != world.wrappingAllowedInY) {
      yWrap = world.wrappingAllowedInY
      buf.addWrapY(yWrap)
    }
    if (!perspective.equals(world.observer.targetAgent, world.observer.perspective)) {
      perspective = new AgentPerspective(world.observer.targetAgent,
          world.observer.perspective, (world.worldWidth - 1) / 2,
          true)
      buf.addPerspective(perspective)
    }
  }

  /**
   * updates local patch data to match patches from world, storing diffs in buf.
   */
  private def updatePatches(world: World, buf: DiffBuffer) {
    // patches can't die, so this is easy...
    for(patch <- world.patches.agents.iterator.asScala.map(_.asInstanceOf[Patch])) {
      val diffs = updatePatch(patch)
      if (diffs != null)
        buf.addPatch(diffs)
    }
  }

  /**
   * updates local turtle data to match turtles from world, storing diffs in buf. Will overwrite
   * turtles with a new map representing the new state of the turtles.
   */
  private def updateTurtles(world: World, buf: DiffBuffer) {
    // turtles, on the other hand, can die, so we move each one to a new map as we encounter it
    val newTurtles = collection.mutable.HashMap[Long, TurtleData]()
    for(turtle <- world.turtles.agents.iterator.asScala.map(_.asInstanceOf[Turtle])) {
      val diffs = updateTurtle(turtle)
      if (diffs != null)
        buf.addTurtle(diffs)
      val tmp = turtles(turtle.id)
      turtles -= turtle.id
      newTurtles(turtle.id) = tmp
    }
    // now, any turtles left in the old map must have died...
    for (turtle <- turtles.values)
      // so, add a new "dead" TurtleData to the outgoing buffer.
      buf.addTurtle(new TurtleData(turtle.id))
    // finally, the new map replaces the old one.
    turtles = newTurtles
  }

  /**
   * updates local link data to match links from world, storing diffs in buf. Will overwrite
   * links with a new map representing the new state of the links.
   */
  private def updateLinks(world: World, buf: DiffBuffer) {
    // links can die too (see comments in updateTurtles)
    val newLinks = collection.mutable.HashMap[ClientWorld.LinkKey, LinkData]()
    for(link <- world.links.agents.iterator.asScala.map(_.asInstanceOf[Link])) {
      val diffs = updateLink(link)
      if (diffs != null)
        buf.addLink(diffs)
      val key = new ClientWorld.LinkKey(link.id, link.end1.id, link.end2.id, link.getBreedIndex)
      val tmp = links(key)
      links -= key
      newLinks(key) = tmp
    }
    for (data <- links.values)
      buf.addLink(new LinkData(data.id))
    links = newLinks
  }

  /**
   * updates local patch data to match a patch from world, storing diffs in a local buffer.
   */
  private def updatePatch(patch: Patch): PatchData = {
    // make a data object for this patch.
    val pd = new PatchData(
      patch.id, PatchData.COMPLETE, patch.pxcor, patch.pycor, patch.pcolor,
      patch.labelString, patch.labelColor)

    // we'll need our version, if we've got one.
    patches.get(patch.id) match {
      // if we haven't got one, this is a new patch...
      case None =>
        patches(patch.id) = pd
        // this patch is complete and new, so it IS the diffs.
        pd
      case Some(bufPatch) =>
        patches(patch.id) = pd
        // otherwise, perform the update...
        bufPatch.updateFrom(pd)
    }
  }

  /**
   * updates local turtle data to match a turtle from world, storing diffs in a local buffer.
   */
  private def updateTurtle(turtle: Turtle): TurtleData = {
    // make a data object for this turtle.
    val td = new TurtleData(
      turtle.id, TurtleData.COMPLETE, turtle.xcor, turtle.ycor,
      turtle.shape, turtle.color, turtle.heading, turtle.size, turtle.hidden,
      turtle.labelString, turtle.labelColor, turtle.getBreedIndex, turtle.lineThickness)
    // we'll need our version, if we've got one.
    turtles.get(turtle.id) match {
      case None =>
        // if we haven't got one, this turtle is new...
        turtles(turtle.id) = td
        // this turtle is complete and new, so it IS the diffs.
        td
     case Some(bufTurtle) =>
       // otherwise, perform the update...
       bufTurtle.updateFrom(td)
    }
  }

  private def updateLink(link: Link): LinkData = {
    // make a data object for this link
    val data = new LinkData(
      link.id, link.end1.id, link.end2.id, LinkData.COMPLETE, link.x1, link.y1, link.x2, link.y2,
      link.shape, AgentData.toLogoList(link.color), link.hidden, link.labelString, AgentData.toLogoList(link.labelColor),
      link.lineThickness, link.isDirectedLink,
      if (link.isDirectedLink) link.linkDestinationSize else 1,
      link.heading, link.size, link.getBreedIndex)
    // we'll need our version, if we've got one.
    links.get(data.getKey) match {
      case None =>
        // if we haven't got one, this link is new...
        links(data.getKey) = data
        // this link is complete and new, so it IS the diffs.
        data
      case Some(bufLink) =>
        // otherwise, perform the update...
        bufLink.updateFrom(data)
    }
  }

  private def updateDrawing(world: World, buf: DiffBuffer, resetWorld: Boolean) {
    world.getDrawing match {
      case d: java.awt.image.BufferedImage =>
        drawing = d
        if (drawing != null && (world.sendPixels || resetWorld)) {
          buf.addDrawing(drawing)
          world.markDrawingClean
        }
      case _ =>
    }
  }

  /**
   * serializes the world data to the given DataOutputStream.
   */
  private def serialize(out: java.io.DataOutputStream) {
    // first the mask. we're just going to dump everything.
    out.writeShort(
      (if (drawing == null)
         DiffBuffer.EVERYTHING
       else
         DiffBuffer.EVERYTHING | DiffBuffer.DRAWING).toShort)
    // then all of the general info.
    out.writeInt(minPxcor)
    out.writeInt(minPycor)
    out.writeInt(maxPxcor)
    out.writeInt(maxPycor)
    out.writeInt(fontSize)
    out.writeBoolean(xWrap)
    out.writeBoolean(yWrap)
    perspective.serialize(out)
    // patches
    out.writeInt(patches.size)
    for(patch <- patches.values)
      patch.serialize(out)
    // turtles
    out.writeInt(turtles.size)
    for (turtle <- turtles.values)
      turtle.serialize(out)
    // links
    out.writeInt(links.size)
    for (link <- links.values)
      link.serialize(out)
    if (drawing != null)
      javax.imageio.ImageIO.write(drawing, "PNG", out)
  }

  /**
   * serializes the world data to a new byte array. this is synchronized
   * since we are accessing the world, patches, and turtles info and we
   * don't want it changing out from underneath us while we are accessing it.
   * 
   * This method MUST be called from the event thread.
   */
  def toByteArray: Array[Byte] = {
    synchronized {
      val bos = new java.io.ByteArrayOutputStream
      serialize(new java.io.DataOutputStream(bos))
      bos.toByteArray
    }
  }

}
