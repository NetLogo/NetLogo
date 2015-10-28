// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.client

import org.nlogo.hubnet.mirroring._
import org.nlogo.render.AbstractRenderer
import org.nlogo.api.{GraphicsInterface, ViewSettings}
import org.nlogo.core.{ AgentKind, ShapeList }

class ClientRenderer(world: ClientWorld) extends AbstractRenderer(world, new ShapeList(AgentKind.Turtle), new ShapeList(AgentKind.Link)) {
  import collection.JavaConverters._
  override def paintTurtles(g: GraphicsInterface, patchSize: Double) {
    for(data <- world.getTurtles.asScala)
      turtleDrawer.drawTurtle(g, topology, data, patchSize)
  }
  override def paintLinks(g: GraphicsInterface, patchSize: Double) {
    for(data <- world.getLinks.asScala)
      linkDrawer.drawLink(g, topology, data, patchSize, false)
  }
  override def paintPatchLabels(g: GraphicsInterface, patchSize: Double) {
    for(data <- world.getPatches)
      if(data.hasLabel)
        drawPatchLabel(g, data, patchSize)
  }
  private def drawPatchLabel(g: GraphicsInterface, patch: PatchData, patchSize: Double) {
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
  def drawLine(line: HubNetLine) {
    trailDrawer.drawLine(line.x1, line.y1, line.x2, line.y2,
                         line.color, line.size, line.mode)
  }
  def stamp(turtle: HubNetTurtleStamp) {
    trailDrawer.stamp(new TurtleData(turtle), turtle.erase)
  }
  def stamp(link: HubNetLinkStamp) {
    trailDrawer.stamp(new LinkData(link), link.erase)
  }
  def clearDrawing() {
    trailDrawer.clearDrawing()
  }
  def paint(g: java.awt.Graphics2D, settings: ViewSettings) { }
  def exportView(g: java.awt.Graphics2D, settings: ViewSettings) = unsupported
  def exportView(settings: ViewSettings): java.awt.image.BufferedImage = unsupported
  def graphicsX(xcor: Double, patchSize: Double, viewOffsetX: Double): Double = unsupported
  def graphicsY(ycor: Double, patchSize: Double, viewOffsetY: Double): Double = unsupported
  private def unsupported = throw new UnsupportedOperationException
}
