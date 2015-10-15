// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo
package org.nlogo.shape

import org.nlogo.api.GraphicsInterface
import org.nlogo.core.Shape, Shape.{ Line => BaseLine }
import java.awt.{ Color, Point, Rectangle => AwtRectangle, geom },
  geom.Line2D
import java.util.StringTokenizer

@SerialVersionUID(0L)
class Line(color: Color) extends Element(color) with BaseLine with Cloneable {

  override def startPoint: (Int, Int) = (start.getX.toInt, start.getY.toInt)

  override def endPoint: (Int, Int) = (end.getX.toInt, end.getY.toInt)

  private var start: Point = null
  private var end: Point = null
  private var modifiedPoint: String = null

  def this(start: Point, last: Point, color: Color) = {
    this(color)
    this.start = start
    end = last
  }

  def getStart: Point = start

  def getEnd: Point = end

  override def clone: AnyRef = {
    val newLine: Line = super.clone.asInstanceOf[Line]
    newLine.start = newLine.start.clone.asInstanceOf[Point]
    newLine.end = newLine.end.clone.asInstanceOf[Point]
    newLine
  }

  def bounds: AwtRectangle = createRect(start, end)

  def contains(p: Point): Boolean =
    new Line2D.Double(start, end).ptSegDist(p) < 3

  def modify(start: Point, last: Point) = {
    end.x = last.x
    end.y = last.y
  }

  def reshapeElement(oldPoint: Point, newPoint: Point) = {
    if (modifiedPoint == "start") {
      start = newPoint
    }
    if (modifiedPoint == "end") {
      end = newPoint
    }
  }

  def moveElement(xOffset: Int, yOffset: Int) = {
    start.x += xOffset
    start.y += yOffset
    end.x += xOffset
    end.y += yOffset
  }

  def rotateLeft() = {
    var temp: Int = start.x
    start.x = start.y
    start.y = Shape.Width - temp
    temp = end.x
    end.x = end.y
    end.y = Shape.Width - temp
  }

  def rotateRight() = {
    var temp: Int = start.x
    start.x = Shape.Width - start.y
    start.y = temp
    temp = end.x
    end.x = Shape.Width - end.y
    end.y = temp
  }

  def flipHorizontal() = {
    start.x = Shape.Width - start.x
    end.x = Shape.Width - end.x
  }

  def flipVertical() = {
    start.y = Shape.Width - start.y
    end.y = Shape.Width - end.y
  }

  def draw(g: GraphicsInterface, turtleColor: Color, scale: Double, angle: Double) = {
    g.setColor(displayColor(turtleColor))
    g.drawLine(start.x, start.y, end.x, end.y)
  }

  def fill(g: GraphicsInterface) = { }

  override def toString: String =
    "Line " + awtColor.getRGB + " " + marked + " " + start.x + " " + start.y + " " + end.x + " " + end.y

  def toReadableString: String =
    "Line with color " + awtColor + " and bounds " + bounds
}

