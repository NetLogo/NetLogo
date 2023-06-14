// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo
package org.nlogo.shape

import java.awt.geom.Ellipse2D

import org.nlogo.api.GraphicsInterface
import java.awt.{ Color, Point, Rectangle => AwtRectangle }

import org.nlogo.core.Shape, Shape.{ Circle => BaseCircle }

@SerialVersionUID(0L)
class Circle(color: Color) extends Element(color) with BaseCircle with Cloneable {
  import Math.round
  var x: Int = 0
  var y: Int = 0

  private var xDiameter: Int = 0
  private var yDiameter: Int = 0

  override def diameter: Int = bounds.getWidth.toInt

  def this(center: Point, circum: Point, color: Color) {
    this(color)
    val radius: Double = distance(center, circum)
    x = center.x - round(radius).toInt
    y = center.y - round(radius).toInt
    xDiameter = round(2.0 * radius).toInt
    yDiameter = xDiameter
  }

  def this(x: Int, y: Int, xDiameter: Int, color: Color) {
    this(color)
    this.x = x
    this.y = y
    this.xDiameter = xDiameter
    yDiameter = xDiameter
  }

  def origin: Point =
    new Point(x + round(xDiameter.toFloat / 2), y + round(yDiameter.toFloat / 2))

  def bounds: AwtRectangle =
    new AwtRectangle(x, y, xDiameter, yDiameter)

  def modify(center: Point, circum: Point) {
    val radius: Double = distance(center, circum)
    x = center.x - round(radius).toInt
    y = center.y - round(radius).toInt
    xDiameter = round(2.0 * radius).toInt
    yDiameter = xDiameter
  }

  def setModifiedPoint(modified: Point): Unit = { }

  def reshapeElement(oldPoint: Point, newPoint: Point) {
    val change: Double = distance(origin, newPoint)
    x = origin.x - change.toInt
    y = origin.y - change.toInt
    xDiameter = change.toInt * 2
    yDiameter = change.toInt * 2
  }

  def moveElement(xOffset: Int, yOffset: Int) {
    x += xOffset
    y += yOffset
  }

  override def getHandles: Array[Point] = {
    val top = new Point(x + (xDiameter / 2), y)
    val left = new Point(x, y + (yDiameter / 2))
    val right = new Point(x + xDiameter, y + (yDiameter / 2))
    val bottom = new Point(x + (xDiameter / 2), y + yDiameter)
    Array[Point](top, left, right, bottom)
  }

  def contains(p: Point): Boolean = {
    val check: Ellipse2D.Double = new Ellipse2D.Double(x, y, xDiameter, yDiameter)
    check.contains(p.x, p.y)
  }

  def draw(g: GraphicsInterface, turtleColor: Color, scale: Double, angle: Double) {
    g.setColor(displayColor(turtleColor))
    if (filled) {
      g.fillCircle(x, y, xDiameter, yDiameter, scale, angle)
    } else {
      g.drawCircle(x, y, xDiameter, yDiameter, scale, angle)
    }
  }

  def rotateLeft() = {
    val oldX: Int = x
    x = y
    y = Shape.Width - oldX - yDiameter
    val oldXDiameter: Int = xDiameter
    xDiameter = yDiameter
    yDiameter = oldXDiameter
  }

  def rotateRight() = {
    val oldX: Int = x
    x = Shape.Width - y - xDiameter
    y = oldX
    val oldXDiameter: Int = xDiameter
    xDiameter = yDiameter
    yDiameter = oldXDiameter
  }

  def flipHorizontal() =
    x = Shape.Width - x - xDiameter

  def flipVertical() =
    y = Shape.Width - y - yDiameter

  def toReadableString: String =
    "Type: Circle, color: " + awtColor + ",\n bounds: " + bounds

  override def toString: String =
    "Circle " + awtColor.getRGB + " " + filled + " " + marked + " " + x + " " + y + " " + xDiameter
}
