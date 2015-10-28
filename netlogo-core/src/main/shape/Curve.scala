// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo
package org.nlogo.shape

import org.nlogo.api.GraphicsInterface
import org.nlogo.core.Shape
import java.awt.{ Color, Point, Rectangle => AwtRectangle }

// Note: currently I think this is used only as an abstract superclass
// for Polygon.  Neither Steph nor I really knows why -- not sure if
// it's just a historical thing or if it actually makes sense that way.
//  - ST 6/11/04
// If this were to become an element type in its own right, then this
// is the method that would be used to draw it:
// drawArc( int x, int y, int width, int height, int startAngle, int arcAngle )
// except this doesn't allow for rotation.  Presumably Graphics2D has a similar
// method that would draw a rotated arc? - SAB/ST 6/11/04
@SerialVersionUID(0L)
abstract class Curve(color: Color) extends Element(color) with Cloneable {
  var xcoords = Array[Int]()
  var ycoords = Array[Int]()
  private var xmin: Int = 0
  private var xmax: Int = 0
  private var ymin: Int = 0
  private var ymax: Int = 0

  def this(start: Point, next: Point, color: Color) = {
    this(color)
    xcoords :+ start.x
    ycoords :+ start.y
    xcoords :+ next.x
    ycoords :+ next.y
    xmin = start.x
    xmax = start.x
    ymin = start.y
    ymax = start.y
    updateBounds(next)
  }

  def bounds: AwtRectangle =
    createRect(new Point(xmin, ymin), new Point(xmax, ymax))

  def modify(start: Point, next: Point) = {
    xcoords :+ next.x
    ycoords :+ next.y
    updateBounds(next)
  }

  def draw(g: GraphicsInterface, turtleColor: Color, scale: Double, angle: Double) = {
    val xArray: Array[Int] = new Array[Int](xcoords.size)
    val yArray: Array[Int] = new Array[Int](xcoords.size)
    for (i <- 0 until xcoords.size) {
      xArray(i) = xcoords(i)
      yArray(i) = ycoords(i)
    }
    g.setColor(awtColor)
    g.drawPolyline(xArray, yArray, xcoords.size)
  }

  def rotateLeft() =
    for (i <- 0 until xcoords.size) {
      val temp: Int = xcoords(i)
      xcoords(i) = ycoords(i)
      ycoords(i) = Shape.Width - temp
    }

  def rotateRight() =
    for (i <- 0 until xcoords.size) {
      val temp: Int = xcoords(i)
      xcoords(i) = Shape.Width - ycoords(i)
      ycoords(i) = temp
    }

  def flipHorizontal() =
    for (i <- 0 until xcoords.size) {
      xcoords(i) = Shape.Width - xcoords(i)
    }

  def flipVertical() =
    for (i <- 0 until ycoords.size)  {
      ycoords(i) = Shape.Width - ycoords(i)
    }

  private def updateBounds(newPoint: Point) = {
    xmin = StrictMath.min(xmin, newPoint.x)
    xmax = StrictMath.max(xmax, newPoint.x)
    ymin = StrictMath.min(ymin, newPoint.y)
    ymax = StrictMath.max(ymax, newPoint.y)
  }

  def toReadableString: String =
    "Type: Curve, color: " + awtColor + ",\n bounds: " + bounds
}
