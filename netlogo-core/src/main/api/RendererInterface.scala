// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait RendererInterface {
  def trailDrawer: TrailDrawerInterface
  def changeTopology(wrapX: Boolean, wrapY: Boolean): Unit
  def paint(g: GraphicsInterface, settings: ViewSettings): Unit
  def paint(g: java.awt.Graphics2D, settings: ViewSettings): Unit
  def resetCache(patchSize: Double): Unit
  def graphicsX(xcor: Double, patchSize: Double, viewOffsetX: Double): Double
  def graphicsY(ycor: Double, patchSize: Double, viewOffsetY: Double): Double
  def outlineAgent(agent: Agent): Unit
  def exportView(g: java.awt.Graphics2D, settings: ViewSettings): Unit
  def exportView(settings: ViewSettings): java.awt.image.BufferedImage
  def prepareToPaint(settings: ViewSettings, width: Int, height: Int): Unit
  def setRenderLabelsAsRectangles(b: Boolean): Unit
}
