// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

// so we can mock Graphics2D for testing

trait GraphicsInterface {
  def antiAliasing(on: Boolean)
  def draw(shape: java.awt.Shape)
  def drawImage(image: java.awt.image.BufferedImage)
  def drawImage(image: java.awt.Image, x: Int, y: Int, width: Int, height: Int)
  def drawLine(x1: Double, y1: Double, x2: Double, y2: Double)
  def drawLabel(s: String, x: Double, y: Double, patchSize: Double)
  def fill(shape: java.awt.Shape)
  def fillRect(x: Int, y: Int, width: Int, height: Int)
  def pop()
  def push()
  def rotate(theta: Double)
  def rotate(theta: Double, x: Double, y: Double)
  def rotate(theta: Double, x: Double, y: Double, offset: Double)
  def scale(x: Double, y: Double)
  def scale(x: Double, y: Double, shapeWidth: Double)
  def setColor(c: java.awt.Color)
  def setComposite(comp: java.awt.Composite)
  def setStroke(width: Double)
  def setStroke(width: Float, dashes: Array[Float])
  def setStrokeFromLineThickness(lineThickness: Double, scale: Double, cellSize: Double, shapeWidth: Double)
  def translate(x: Double, y: Double)
  def setInterpolation()
  def setStrokeControl()
  def drawPolygon(xcors: Array[Int], ycors: Array[Int], length: Int)
  def fillPolygon(xcors: Array[Int], ycors: Array[Int], length: Int)
  def drawPolyline(xcors: Array[Int], ycors: Array[Int], length: Int)
  def dispose()
  def location(x: Double, y: Double): String
  def fillCircle(x: Double, y: Double, xDiameter: Double, yDiameter: Double, scale: Double, angle: Double)
  def drawCircle(x: Double, y: Double, xDiamter: Double, yDiameter: Double, scale: Double, angle: Double)
  def fillRect(x: Double, y: Double, width: Double, height: Double, scale: Double, angle: Double)
  def drawRect(x: Double, y: Double, width: Double, height: Double, scale: Double, angle: Double)
  def getFontMetrics: java.awt.FontMetrics
}

