// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.File

trait TrailDrawerInterface extends DrawingInterface {
  def drawLine(x0: Double, y0: Double, x1: Double, y1: Double, color: AnyRef, size: Double, mode: String)
  def setColors(colors: Array[Int], width: Int, height: Int)
  def getDrawing: AnyRef
  def sendPixels: Boolean
  def sendPixels(dirty: Boolean)
  def stamp(agent: Agent, erase: Boolean)
  @throws(classOf[java.io.IOException])
  def readImage(is: java.awt.image.BufferedImage)
  @throws(classOf[java.io.IOException])
  def readImage(is: java.io.InputStream)
  @throws(classOf[java.io.IOException])
  def importDrawing(is: java.io.InputStream, mimeTypeOpt: Option[String] = None)
  @throws(classOf[java.io.IOException])
  def importDrawing(file: File)
  @throws(classOf[java.io.IOException])
  def importDrawingBase64(base64: String)
  def getAndCreateDrawing(dirty: Boolean): java.awt.image.BufferedImage
  def clearDrawing()
  def exportDrawingToCSV(writer: java.io.PrintWriter)
  def rescaleDrawing()
  def isDirty: Boolean
}
