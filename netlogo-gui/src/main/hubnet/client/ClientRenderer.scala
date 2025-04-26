// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.client

import org.nlogo.hubnet.mirroring._
import org.nlogo.render.AbstractRenderer
import org.nlogo.api.{ GraphicsInterface, ViewSettings }
import org.nlogo.core.{ AgentKind, ShapeListTracker }

class ClientRenderer(world: ClientWorld) extends AbstractRenderer(world, new ShapeListTracker(AgentKind.Turtle), new ShapeListTracker(AgentKind.Link)) {
  import scala.jdk.CollectionConverters.IterableHasAsScala
  override def paintTurtles(g: GraphicsInterface, patchSize: Double): Unit = {
    for(data <- world.getTurtles.asScala)
      turtleDrawer.drawTurtle(g, topology, data, patchSize)
  }
  override def paintLinks(g: GraphicsInterface, patchSize: Double): Unit = {
    for(data <- world.getLinks.asScala)
      linkDrawer.drawLink(g, topology, data, patchSize, false)
  }
  override def paintPatchLabels(g: GraphicsInterface, patchSize: Double): Unit = {
    for(data <- world.getPatches)
      if(data.hasLabel)
        drawPatchLabel(g, data, patchSize)
  }
  private def drawPatchLabel(g: GraphicsInterface, patch: PatchData, patchSize: Double): Unit = {
    topology.drawLabelHelper(g, patch.pxcor, patch.pycor,
                             patch.plabel, patch.plabelColor,
                             patchSize, 1)
  }
  override def anyTurtles:Boolean =
    world.getTurtles.asScala.isEmpty
  override def getSpotlightImage(settings: ViewSettings): java.awt.image.BufferedImage =
    spotlightDrawer.getImage(topology,
                             world.targetAgent.xcor, world.targetAgent.ycor,
                             getWidth(settings.patchSize), getHeight(settings.patchSize),
                             settings.patchSize,
                             world.targetAgent.spotlightSize,
                             darkenPeripheral(settings),
                             world.targetAgent.wrapSpotlight)
  def drawLine(line: HubNetLine): Unit = {
    trailDrawer.drawLine(line.x1, line.y1, line.x2, line.y2,
                         line.color, line.size, line.mode)
  }
  def stamp(turtle: HubNetTurtleStamp): Unit = {
    trailDrawer.stamp(new TurtleData(turtle), turtle.erase)
  }
  def stamp(link: HubNetLinkStamp): Unit = {
    trailDrawer.stamp(new LinkData(link), link.erase)
  }
  def clearDrawing(): Unit = {
    trailDrawer.clearDrawing()
  }
  def paint(g: java.awt.Graphics2D, settings: ViewSettings): Unit = { }
  def exportView(g: java.awt.Graphics2D, settings: ViewSettings) = unsupported
  def exportView(settings: ViewSettings): java.awt.image.BufferedImage = unsupported
  def graphicsX(xcor: Double, patchSize: Double, viewOffsetX: Double): Double = unsupported
  def graphicsY(ycor: Double, patchSize: Double, viewOffsetY: Double): Double = unsupported
  private def unsupported = throw new UnsupportedOperationException
}
