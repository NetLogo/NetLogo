// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait HeadlessRendererInterface {
  def trailDrawer: TrailDrawerInterface
  def changeTopology(wrapX: Boolean, wrapY: Boolean)
  def resetCache(patchSize: Double)
  def exportView(settings: ViewSettings): java.awt.image.BufferedImage
  def exportView(g: java.awt.Graphics2D, settings: ViewSettings)
  def renderLabelsAsRectangles_=(b: Boolean)
}

trait RendererInterface extends HeadlessRendererInterface {
  def paint(g: GraphicsInterface, settings: ViewSettings)
  def paint(g: java.awt.Graphics2D, settings: ViewSettings)
  def graphicsX(xcor: Double, patchSize: Double, viewOffsetX: Double): Double
  def graphicsY(ycor: Double, patchSize: Double, viewOffsetY: Double): Double
  def outlineAgent(agent: Agent)
  def prepareToPaint(settings: ViewSettings, width: Int, height: Int)
}
