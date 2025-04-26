// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

// so we can mock Graphics2D for testing

trait GraphicsInterface {
  def antiAliasing(on: Boolean): Unit
  def draw(shape: java.awt.Shape): Unit
  def drawImage(image: java.awt.image.BufferedImage): Unit
  def drawImage(image: java.awt.Image, x: Int, y: Int, width: Int, height: Int): Unit
  def drawLine(x1: Double, y1: Double, x2: Double, y2: Double): Unit
  def drawLabel(s: String, x: Double, y: Double, patchSize: Double): Unit
  def fill(shape: java.awt.Shape): Unit
  def fillRect(x: Int, y: Int, width: Int, height: Int): Unit
  def pop(): Unit
  def push(): Unit
  def rotate(theta: Double): Unit
  def rotate(theta: Double, x: Double, y: Double): Unit
  def rotate(theta: Double, x: Double, y: Double, offset: Double): Unit
  def scale(x: Double, y: Double): Unit
  def scale(x: Double, y: Double, shapeWidth: Double): Unit
  def setColor(c: java.awt.Color): Unit
  def setComposite(comp: java.awt.Composite): Unit
  def setStroke(width: Double): Unit
  def setStroke(width: Float, dashes: Array[Float]): Unit
  def setStrokeFromLineThickness(lineThickness: Double, scale: Double, cellSize: Double, shapeWidth: Double): Unit
  def translate(x: Double, y: Double): Unit
  def setInterpolation(): Unit
  def setStrokeControl(): Unit
  def drawPolygon(xcors: Array[Int], ycors: Array[Int], length: Int): Unit
  def fillPolygon(xcors: Array[Int], ycors: Array[Int], length: Int): Unit
  def drawPolyline(xcors: Array[Int], ycors: Array[Int], length: Int): Unit
  def dispose(): Unit
  def location(x: Double, y: Double): String
  def fillCircle(x: Double, y: Double, xDiameter: Double, yDiameter: Double, scale: Double, angle: Double): Unit
  def drawCircle(x: Double, y: Double, xDiamter: Double, yDiameter: Double, scale: Double, angle: Double): Unit
  def fillRect(x: Double, y: Double, width: Double, height: Double, scale: Double, angle: Double): Unit
  def drawRect(x: Double, y: Double, width: Double, height: Double, scale: Double, angle: Double): Unit
  def getFontMetrics: java.awt.FontMetrics
}
