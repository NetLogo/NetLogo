// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo
package org.nlogo.shape

import org.nlogo.api.GraphicsInterface
import java.awt.{ Color, Point, Rectangle => AwtRectangle }
import org.nlogo.core.Shape, Shape.{ RgbColor, Element => BaseElement }

@SerialVersionUID(0L)
object Element {
  private[shape] val SHAPE_WIDTH: Int = Shape.Width

  def round(d: Double): Int =
    StrictMath.rint(d).toInt

  protected def ceiling(d: Double): Int =
    StrictMath.ceil(d).toInt
}

@SerialVersionUID(0L)
abstract class Element(var awtColor: Color = null) extends BaseElement with java.io.Serializable with Cloneable {
  var filled: Boolean = false

  var marked: Boolean = false

  def color: RgbColor = RgbColor(awtColor.getRed, awtColor.getGreen, awtColor.getBlue, awtColor.getAlpha)

  protected var selected: Boolean = false

  def displayColor(turtleColor: Color): Color =
    if (marked && turtleColor != null)
      turtleColor
    else if (turtleColor == null || awtColor.getAlpha == turtleColor.getAlpha)
      awtColor
    else
      new Color(awtColor.getRed, awtColor.getGreen, awtColor.getBlue, turtleColor.getAlpha)

  def bounds: AwtRectangle

  def getHandles: Array[Point]

  def toReadableString: String

  def modify(start: Point, last: Point)

  def draw(g: GraphicsInterface, turtleColor: Color, scale: Double, angle: Double)

  def rotateLeft()

  def rotateRight()

  def flipHorizontal()

  def flipVertical()

  override def clone: AnyRef =
    try {
      super.clone
    } catch {
      case ex: CloneNotSupportedException => throw new IllegalStateException(ex)
    }

  def reshapeElement(oldPoint: Point, newPoint: Point)

  def setModifiedPoint(modified: Point)

  def contains(p: Point): Boolean

  def moveElement(xOffset: Int, yOffset: Int)

  private[shape] def shouldSave: Boolean = {
    true
  }

  protected def createRect(start: Point, end: Point): AwtRectangle =
    new AwtRectangle(
      StrictMath.min(start.x, end.x),
      StrictMath.min(start.y, end.y),
      StrictMath.abs(start.x - end.x),
      StrictMath.abs(start.y - end.y))

  protected def rotatePoint(point: Point, pivot: Point, angle: Int): Point = {
    val rotatedPoint = new Point(point.x, 2 * pivot.y - point.y)
    val radius: Double = distance(rotatedPoint, pivot)
    if (radius == 0)
      return rotatedPoint
    var newAngle: Double = StrictMath.atan((rotatedPoint.y - pivot.y).toDouble / (rotatedPoint.x - pivot.x).toDouble)
    if (rotatedPoint.x < pivot.x)
      newAngle += StrictMath.PI
    if (newAngle < 0)
      newAngle += 2 * StrictMath.PI
    newAngle -= 2 * StrictMath.PI * angle / 360
    val newx: Double = pivot.x + (radius * StrictMath.cos(newAngle))
    var newy: Double = pivot.y + (radius * StrictMath.sin(newAngle))
    newy = (2 * pivot.y) - newy
    new Point(StrictMath.rint(newx).toInt, StrictMath.rint(newy).toInt)
  }

  protected def distance(center: Point, circum: Point): Double =
    StrictMath.sqrt(StrictMath.pow(center.y - circum.y, 2.0) + StrictMath.pow(center.x - circum.x, 2.0))

  protected def min(array: Array[Int]): Int = array.min

  protected def max(array: Array[Int]): Int = array.max

  def select() = selected = true

  def deselect() = selected = false
}
