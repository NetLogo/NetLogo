// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait TrailDrawerInterface extends DrawingInterface {
  def drawLine(x0: Double, y0: Double, x1: Double, y1: Double, color: AnyRef, size: Double, mode: String)
  def setColors(colors: Array[Int])
  def getDrawing: AnyRef
  def sendPixels: Boolean
  def sendPixels(dirty: Boolean)
  def stamp(agent: Agent, erase: Boolean)
  @throws(classOf[java.io.IOException])
  def readImage(is: java.io.InputStream)
  @throws(classOf[java.io.IOException])
  def importDrawing(file: File)
  def getAndCreateDrawing(dirty: Boolean): java.awt.image.BufferedImage
  def clearDrawing()
  def exportDrawingToCSV(writer: java.io.PrintWriter)
  def rescaleDrawing()
  def isDirty: Boolean
}
