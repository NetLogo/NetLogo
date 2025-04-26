// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.File

trait TrailDrawerInterface extends DrawingInterface {
  def drawLine(x0: Double, y0: Double, x1: Double, y1: Double, color: AnyRef, size: Double, mode: String): Unit
  def setColors(colors: Array[Int], width: Int, height: Int): Unit
  def getDrawing: AnyRef
  def sendPixels: Boolean
  def sendPixels(dirty: Boolean): Unit
  def stamp(agent: Agent, erase: Boolean): Unit
  @throws(classOf[java.io.IOException])
  def readImage(is: java.awt.image.BufferedImage): Unit
  @throws(classOf[java.io.IOException])
  def readImage(is: java.io.InputStream): Unit
  @throws(classOf[java.io.IOException])
  def importDrawing(is: java.io.InputStream, mimeTypeOpt: Option[String] = None): Unit
  @throws(classOf[java.io.IOException])
  def importDrawing(file: File): Unit
  @throws(classOf[java.io.IOException])
  def importDrawingBase64(base64: String): Unit
  def getAndCreateDrawing(dirty: Boolean): java.awt.image.BufferedImage
  def clearDrawing(): Unit
  def exportDrawingToCSV(writer: java.io.PrintWriter): Unit
  def rescaleDrawing(): Unit
  def isDirty: Boolean
}
