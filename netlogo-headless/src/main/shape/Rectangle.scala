// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo
package org.nlogo.shape

import org.nlogo.api.GraphicsInterface
import org.nlogo.core.Shape, Shape.{ Rectangle => BaseRectangle }
import java.awt.{Point, Color, Rectangle => AwtRectangle}
import java.util.StringTokenizer

@SerialVersionUID(0L)
class Rectangle(color: Color) extends Element(color) with BaseRectangle with Cloneable {

  def this(start: Point, end: Point, color: Color) = {
    this(color)
    upperLeft = new Point(start)
    upperRight = new Point(end.x, start.y)
    lowerLeft = new Point(start.x, end.y)
    lowerRight = new Point(end)
  }

  protected var upperLeft: Point = null
  protected var upperRight: Point = null
  protected var lowerRight: Point = null
  protected var lowerLeft: Point = null
  protected var xmin: Int = 0
  protected var xmax: Int = 0
  protected var ymin: Int = 0
  protected var ymax: Int = 0
  private var modifiedPoint: String = null

  override def getX: Int =
    upperLeft.x

  override def getY: Int =
    upperLeft.y

  override def getWidth: Int =
    lowerRight.x - upperLeft.x

  override def getHeight: Int =
    lowerRight.y - upperLeft.y

  override def upperLeftCorner: (Int, Int) =
    (upperLeft.getX.toInt, upperLeft.getY.toInt)

  override def lowerRightCorner: (Int, Int) =
    (lowerRight.getX.toInt, lowerRight.getY.toInt)

  def getCorners: Array[Point] =
    Array[Point](upperLeft, lowerRight)

  override def clone: AnyRef = {
    val newRect: Rectangle = super.clone.asInstanceOf[Rectangle]
    newRect.upperLeft = newRect.upperLeft.clone.asInstanceOf[Point]
    newRect.upperRight = newRect.upperRight.clone.asInstanceOf[Point]
    newRect.lowerLeft = newRect.lowerLeft.clone.asInstanceOf[Point]
    newRect.lowerRight = newRect.lowerRight.clone.asInstanceOf[Point]
    newRect
  }

  def bounds: AwtRectangle = {
    setMaxsAndMins()
    new AwtRectangle(xmin, ymin, xmax - xmin, ymax - ymin)
  }

  def contains(p: Point): Boolean = bounds.contains(p)

  def modify(start: Point, last: Point) = {
    val width: Int = StrictMath.abs(start.x - last.x)
    val height: Int = StrictMath.abs(start.y - last.y)
    upperLeft.x = StrictMath.min(start.x, last.x)
    upperLeft.y = StrictMath.min(start.y, last.y)
    upperRight.x = upperLeft.x + width
    upperRight.y = upperLeft.y
    lowerRight.x = upperLeft.x + width
    lowerRight.y = upperLeft.y + height
    lowerLeft.x = upperLeft.x
    lowerLeft.y = upperLeft.y + height
  }

  def reshapeElement(oldPoint: Point, newPoint: Point) = {
    if (modifiedPoint == "upperLeft") {
      upperLeft = newPoint
      lowerLeft.x = newPoint.x
      upperRight.y = newPoint.y
    }
    if (modifiedPoint == "upperRight") {
      upperRight = newPoint
      lowerRight.x = newPoint.x
      upperLeft.y = newPoint.y
    }
    if (modifiedPoint == "lowerRight") {
      lowerRight = newPoint
      upperRight.x = newPoint.x
      lowerLeft.y = newPoint.y
    }
    if (modifiedPoint == "lowerLeft") {
      lowerLeft = newPoint
      upperLeft.x = newPoint.x
      lowerRight.y = newPoint.y
    }
    xmin = upperLeft.x
    xmax = upperRight.x
    ymin = upperLeft.y
    ymax = lowerLeft.y
  }

  def moveElement(xOffset: Int, yOffset: Int) = {
    upperLeft.x += xOffset
    upperLeft.y += yOffset
    upperRight.x += xOffset
    upperRight.y += yOffset
    lowerLeft.x += xOffset
    lowerLeft.y += yOffset
    lowerRight.x += xOffset
    lowerRight.y += yOffset
  }

  def setMaxsAndMins() = {
    val xcoords: Array[Int] = Array(upperLeft.x, upperRight.x, lowerRight.x, lowerLeft.x)
    val ycoords: Array[Int] = Array(upperLeft.y, upperRight.y, lowerRight.y, lowerLeft.y)
    xmin = min(xcoords)
    xmax = max(xcoords)
    ymin = min(ycoords)
    ymax = max(ycoords)
  }

  def draw(g: GraphicsInterface, turtleColor: Color, scale: Double, angle: Double) = {
    g.setColor(displayColor(turtleColor))
    if (filled)
      g.fillRect(upperLeft.x, upperLeft.y, upperRight.x - upperLeft.x, lowerLeft.y - upperLeft.y, scale, angle)
    else
      g.drawRect(upperLeft.x, upperLeft.y, upperRight.x - upperLeft.x, lowerLeft.y - upperLeft.y, scale, angle)
  }

  def rotateLeft() = {
    val temp: Point = lowerLeft
    lowerLeft = upperLeft
    upperLeft = upperRight
    upperRight = lowerRight
    lowerRight = temp
    var temp2: Int = 0
    temp2 = upperLeft.x
    upperLeft.x = upperLeft.y
    upperLeft.y = Shape.Width - temp2
    temp2 = upperRight.x
    upperRight.x = upperRight.y
    upperRight.y = Shape.Width - temp2
    temp2 = lowerLeft.x
    lowerLeft.x = lowerLeft.y
    lowerLeft.y = Shape.Width - temp2
    temp2 = lowerRight.x
    lowerRight.x = lowerRight.y
    lowerRight.y = Shape.Width - temp2
  }

  def rotateRight() = {
    val temp: Point = upperLeft
    upperLeft = lowerLeft
    lowerLeft = lowerRight
    lowerRight = upperRight
    upperRight = temp
    var temp2: Int = 0
    temp2 = upperLeft.x
    upperLeft.x = Shape.Width - upperLeft.y
    upperLeft.y = temp2
    temp2 = lowerLeft.x
    lowerLeft.x = Shape.Width - lowerLeft.y
    lowerLeft.y = temp2
    temp2 = upperRight.x
    upperRight.x = Shape.Width - upperRight.y
    upperRight.y = temp2
    temp2 = lowerRight.x
    lowerRight.x = Shape.Width - lowerRight.y
    lowerRight.y = temp2
  }

  def flipHorizontal() = {
    var temp: Point = upperLeft
    upperLeft = upperRight
    upperRight = temp
    temp = lowerLeft
    lowerLeft = lowerRight
    lowerRight = temp
    upperLeft.x = Shape.Width - upperLeft.x
    upperRight.x = Shape.Width - upperRight.x
    lowerLeft.x = Shape.Width - lowerLeft.x
    lowerRight.x = Shape.Width - lowerRight.x
  }

  def flipVertical() = {
    var temp: Point = upperLeft
    upperLeft = lowerLeft
    lowerLeft = temp
    temp = lowerRight
    lowerRight = upperRight
    upperRight = temp
    upperLeft.y = Shape.Width - upperLeft.y
    upperRight.y = Shape.Width - upperRight.y
    lowerLeft.y = Shape.Width - lowerLeft.y
    lowerRight.y = Shape.Width - lowerRight.y
  }

  def toReadableString: String =
    s"Type: Rectangle, color: $awtColor,\n bounds: $bounds"

  override def toString: String =
    s"Rectangle ${awtColor.getRGB} $filled $marked ${upperLeft.x} ${upperLeft.y} ${lowerRight.x} ${lowerRight.y}"
}
