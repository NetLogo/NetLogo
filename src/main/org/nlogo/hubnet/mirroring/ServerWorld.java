// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.mirroring;

import org.nlogo.api.Agent;
import org.nlogo.api.Link;
import org.nlogo.api.Patch;
import org.nlogo.api.Turtle;
import org.nlogo.api.World;
import org.nlogo.api.WorldPropertiesInterface;

import java.util.HashMap;
import java.util.Map;

/**
 * This class implements a server-side cache of view mirroring-related world
 * data. view updates are incremental whenever possible, so we keep track here
 * of the state of the view as of the last client update. To do a new update,
 * we compare this with the actual state of the world, record updates here and
 * compile a list of differences to send to clients. See updateWorld() below.
 * <p/>
 * This class also supports serialization directly to a byte array, which is
 * used in those cases when we need a full (i.e. non-incremental) view update.
 * Newly entered clients, for instance, will therefore be send a copy of the
 * view data as it was last sent to the other clients. See toByteArray() below.
 * <p/>
 * In order to be thread safe, the policy for this class is that all methods
 * must be called from the event thread.  Further, for some of the methods, we
 * require that it is safe for us to lock on the world.  In particular, any
 * code that accesses the current state of the world must have a lock on the
 * world.  All such methods are commented.  To ensure that we are on the event
 * thread, we check that we are on all methods which act as entry points into
 * an instance of this class.
 */
public strictfp class ServerWorld {
  private final WorldPropertiesInterface settings;

  private Map<Double, TurtleData> turtles;
  private final Map<Double, PatchData> patches;
  private Map<ClientWorld.LinkKey, LinkData> links;

  private int minPxcor;
  private int minPycor;
  private int maxPxcor;
  private int maxPycor;
  private int fontSize;
  private boolean xWrap;
  private boolean yWrap;
  private AgentPerspective perspective
      = new AgentPerspective(null, 0, 0, -1, true);

  /**
   * whether turtle shapes should be displayed.
   */
  private boolean shapes;


  private java.awt.image.BufferedImage drawing = null;

  /**
   * creates a new ServerWorld. manager will be used to convert shapes to
   * shape indices.
   */
  public ServerWorld(WorldPropertiesInterface settings) {
    this.settings = settings;
    turtles = new HashMap<Double, TurtleData>();
    patches = new HashMap<Double, PatchData>();
    links = new HashMap<ClientWorld.LinkKey, LinkData>();
  }

  /**
   * updates all local world data to match world, constructing diffs in the
   * process. This is synchronized because it inspects, modifies and may
   * overwrite patches and turtles. This should be fine since it should
   * only ever be called from a single thread.
   * <p/>
   * This method MUST be called from the event thread, and it must be safe
   * for us to get a lock on the world.
   *
   * @return a new DiffBuffer containing the differences between local world
   *         data and that in world.
   */
  public synchronized DiffBuffer updateWorld(World world, boolean resetWorld) {
    DiffBuffer buf = new DiffBuffer();
    synchronized (world) {
      updateGeneral(world, buf);
      updatePatches(world, buf);
      updateTurtles(world, buf);
      updateLinks(world, buf);
      updateDrawing(world, buf, resetWorld);
    }
    return buf;
  }

  /**
   * updates local general view data (sex/y, label font size, etc.) to match
   * the current state of the world, storing diffs in buf.
   * <p/>
   * This method MUST be called from the event thread, and the method which
   * called it must have a lock on the world.
   */
  private void updateGeneral(World world, DiffBuffer buf) {
    if (minPxcor != world.minPxcor()) {
      minPxcor = world.minPxcor();
      buf.addMinX(minPxcor);
    }
    if (minPycor != world.minPycor()) {
      minPycor = world.minPycor();
      buf.addMinY(minPycor);
    }
    if (maxPxcor != world.maxPxcor()) {
      maxPxcor = world.maxPxcor();
      buf.addMaxX(maxPxcor);
    }
    if (maxPycor != world.maxPycor()) {
      maxPycor = world.maxPycor();
      buf.addMaxY(maxPycor);
    }
    if (fontSize != settings.fontSize()) {
      fontSize = settings.fontSize();
      buf.addFontSize(fontSize);
    }
    if (xWrap != world.wrappingAllowedInX()) {
      xWrap = world.wrappingAllowedInX();
      buf.addWrapX(xWrap);
    }
    if (yWrap != world.wrappingAllowedInY()) {
      yWrap = world.wrappingAllowedInY();
      buf.addWrapY(yWrap);
    }
    if (!perspective.equals(world.observer().targetAgent(), world.observer().perspective())) {
      perspective = new AgentPerspective(world.observer().targetAgent(),
          world.observer().perspective(), (world.worldWidth() - 1) / 2,
          true);
      buf.addPerspective(perspective);
    }
  }

  /**
   * updates local patch data to match patches from world, storing
   * diffs in buf.
   * <p/>
   * This method MUST be called from the event thread, and the method which
   * called it must have a lock on the world.
   */
  private void updatePatches(World world, DiffBuffer buf) {
    // patches can't die, so this is easy...
    for (Agent a : world.patches().agents()) {
      PatchData diffs = updatePatch((Patch) a);
      if (diffs != null) {
        buf.addPatch(diffs);
      }
    }
  }

  /**
   * updates local turtle data to match turtles from world, storing
   * diffs in buf. Will overwrite turtles with a new Map representing
   * the new state of the turtles.
   * <p/>
   * This method MUST be called from the event thread, and the method which
   * called it must have a lock on the world.
   */
  private void updateTurtles(World world, DiffBuffer buf) {
    // turtles, on the other hand, can die, so we move each one to a new
    // map as we encounter it...
    Map<Double, TurtleData> newTurtles = new HashMap<Double, TurtleData>();
    for (Agent a : world.turtles().agents()) {
      Turtle turtle = (Turtle) a;
      TurtleData diffs = updateTurtle(turtle);
      if (diffs != null) {
        buf.addTurtle(diffs);
      }
      TurtleData tmp = turtles.remove(Double.valueOf(turtle.id()));
      newTurtles.put(Double.valueOf(turtle.id()), tmp);
    }
    // now, any turtles left in the old map must have died...
    for (TurtleData turtle : turtles.values()) {
      // so, add a new "dead" TurtleData to the outgoing buffer.
      buf.addTurtle(new TurtleData(turtle.id()));
    }
    // finally, the new map replaces the old one.
    turtles = newTurtles;
  }

  private void updateLinks(World world, DiffBuffer buf) {
    // turtles, on the other hand, can die, so we move each one to a new
    // map as we encounter it...
    Map<ClientWorld.LinkKey, LinkData> newLinks = new HashMap<ClientWorld.LinkKey, LinkData>();
    for (Agent a : world.links().agents()) {
      Link link = (Link) a;
      LinkData diffs = updateLink(link);
      if (diffs != null) {
        buf.addLink(diffs);
      }
      ClientWorld.LinkKey key =
          new ClientWorld.LinkKey(link.id(), link.end1().id(), link.end2().id(), link.getBreedIndex());
      LinkData tmp = links.remove(key);
      newLinks.put(key, tmp);
    }
    // now, any link left in the old map must have died...
    for (LinkData data : links.values()) {
      // so, add a new "dead" LinkData to the outgoing buffer.
      buf.addLink(new LinkData(data.id));
    }
    // finally, the new map replaces the old one.
    links = newLinks;
  }

  /**
   * updates local patch data to match a patch from world, storing
   * diffs in a local buffer.
   * <p/>
   * This method MUST be called from the event thread, and the method which
   * called it must have a lock on the world.
   */
  private PatchData updatePatch(Patch patch) {
    // make a data object for this patch.
    PatchData pd = new PatchData(patch.id(), PatchData.COMPLETE,
        patch.pxcor(), patch.pycor(),
        patch.pcolor(), patch.labelString(),
        patch.labelColor());

    // we'll need our version, if we've got one.
    PatchData bufPatch = patches.get(Double.valueOf(patch.id()));

    // if we haven't got one, this is a new patch...
    if (bufPatch == null) {
      patches.put(Double.valueOf(patch.id()), pd);
      // this patch is complete and new, so it IS the diffs.
      return pd;
    }

    patches.put(Double.valueOf(patch.id()), pd);

    // otherwise, perform the update...
    return bufPatch.updateFrom(pd);
  }


  /**
   * updates local turtle data to match a turtle from world, storing
   * diffs in a local buffer.
   * <p/>
   * This method MUST be called from the event thread, and the method which
   * called it must have a lock on the world.
   */
  private TurtleData updateTurtle(Turtle turtle) {
    // make a data object for this turtle.
    TurtleData td = new TurtleData
        (turtle.id(), TurtleData.COMPLETE, turtle.xcor(), turtle.ycor(),
            turtle.shape(), turtle.color(),
            turtle.heading(), turtle.size(), turtle.hidden(),
            turtle.labelString(), turtle.labelColor(), turtle.getBreedIndex(),
            turtle.lineThickness());

    // we'll need our version, if we've got one.
    TurtleData bufTurtle = turtles.get(Double.valueOf(turtle.id()));

    // if we haven't got one, this turtle is new...
    if (bufTurtle == null) {
      turtles.put(Double.valueOf(turtle.id()), td);
      // this turtle is complete and new, so it IS the diffs.
      return td;
    }

    // otherwise, perform the update...
    return bufTurtle.updateFrom(td);
  }

  private LinkData updateLink(Link link) {
    // make a data object for this turtle.
    LinkData data = new LinkData
        (link.id(), link.end1().id(), link.end2().id(), LinkData.COMPLETE, link.x1(), link.y1(), link.x2(), link.y2(),
            link.shape(), link.color(), link.hidden(), link.labelString(), link.labelColor(),
            link.lineThickness(), link.isDirectedLink(), (link.isDirectedLink() ? link.linkDestinationSize() : 1),
            link.heading(), link.size(), link.getBreedIndex());

    // we'll need our version, if we've got one.
    LinkData bufLink = links.get(data.getKey());

    // if we haven't got one, this turtle is new...
    if (bufLink == null) {
      links.put(data.getKey(), data);
      // this turtle is complete and new, so it IS the diffs.
      return data;
    }

    // otherwise, perform the update...
    return bufLink.updateFrom(data);
  }

  private void updateDrawing(World world, DiffBuffer buf, boolean resetWorld) {
    Object obj = world.getDrawing();
    if (obj instanceof java.awt.image.BufferedImage) {
      drawing = (java.awt.image.BufferedImage) obj;
      if (drawing != null && (world.sendPixels() || resetWorld)) {
        buf.addDrawing(drawing);
        world.markDrawingClean();
      }
    }
  }

  /**
   * serializes the world data to the given DataOutputStream.
   * <p/>
   * This method MUST be called from the event thread.
   */
  private void serialize(java.io.DataOutputStream os)
      throws java.io.IOException {
    // first the mask. we're just going to dump everything.
    short mask = DiffBuffer.EVERYTHING;
    if (drawing != null) {
      mask |= DiffBuffer.DRAWING;
    }
    os.writeShort(mask);

    // then all of the general info.
    os.writeInt(minPxcor);
    os.writeInt(minPycor);
    os.writeInt(maxPxcor);
    os.writeInt(maxPycor);
    os.writeBoolean(shapes);
    os.writeInt(fontSize);
    os.writeBoolean(xWrap);
    os.writeBoolean(yWrap);
    perspective.serialize(os);

    // dump patches first.
    os.writeInt(patches.size());
    for (PatchData patch : patches.values()) {
      patch.serialize(os);
    }
    // then dump turtles.
    os.writeInt(turtles.size());
    for (TurtleData turtle : turtles.values()) {
      turtle.serialize(os);
    }
    // then dump links
    os.writeInt(links.size());
    for (LinkData link : links.values()) {
      link.serialize(os);
    }
    if (drawing != null) {
      javax.imageio.ImageIO.write(drawing, "PNG", os);
    }
  }

  /**
   * serializes the world data to a new byte array. this is synchronized
   * since we are accessing the world, patches, and turtles info and we
   * don't want it changing out from underneath us while we are accessing it.
   * <p/>
   * This method MUST be called from the event thread.
   */
  public synchronized byte[] toByteArray() {
    java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
    try {
      serialize(new java.io.DataOutputStream(bos));
    } catch (java.io.IOException e) {
      // shouldn't happen, since we're writing to a byte array...
      throw new IllegalStateException(e);
    }
    // will be empty if an exception occurred, which is what we want...
    return bos.toByteArray();
  }
}
