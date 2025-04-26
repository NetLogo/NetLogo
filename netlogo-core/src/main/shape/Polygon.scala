// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo
package org.nlogo.shape

import java.awt.{ Color, Point, Polygon => AwtPolygon, Rectangle => AwtRectangle }

import org.nlogo.api.GraphicsInterface
import org.nlogo.core.Shape.{ Polygon => BasePolygon }

import scala.collection.immutable.ArraySeq

@SerialVersionUID(0L)
class Polygon(color: Color) extends Curve(color) with BasePolygon with Cloneable {

  override def points: Seq[(Int, Int)] = (xcoords zip ycoords).toIndexedSeq

  private var modifiedPointIndex: Int = -1

  def this(xcoords: List[Int], ycoords: List[Int], c: Color) = {
    this(c)
    this.xcoords = xcoords.toArray
    this.ycoords = ycoords.toArray
  }

  def this(start: Point, color: Color) = {
    this(color)
    notCompleted = true
    xcoords = xcoords :+ start.x
    ycoords = ycoords :+ start.y
    xcoords = xcoords :+ start.x
    ycoords = ycoords :+ start.y
    modifiedPointIndex += 2
  }

  def this(color: Color, filled: Boolean, marked: Boolean, points: List[Point]) = {
    this(color)
    this.filled = filled
    this.marked = marked
    points.foreach(addNewPoint)
  }

  override def clone: AnyRef = {
    val newPoly: Polygon = super.clone.asInstanceOf[Polygon]
    newPoly.xcoords = Array(ArraySeq.unsafeWrapArray(newPoly.xcoords): _*)
    newPoly.ycoords = Array(ArraySeq.unsafeWrapArray(newPoly.ycoords): _*)
    newPoly
  }

  override def bounds: AwtRectangle = {
    new AwtPolygon(xcoords, ycoords, xcoords.size).getBounds
  }

  def addNewPoint(newPoint: Point) = {
    xcoords = xcoords :+ newPoint.x
    ycoords = ycoords :+ newPoint.y
    modifiedPointIndex += 1
  }

  def modifyPoint(newPoint: Point) = {
    xcoords(modifiedPointIndex) = newPoint.x
    ycoords(modifiedPointIndex) = newPoint.y
  }

  override def modify(start: Point, end: Point) = {
    xcoords(modifiedPointIndex) = end.x
    ycoords(modifiedPointIndex) = end.y
  }

  def reshapeElement(oldPoint: Point, newPoint: Point) = {
    if (modifiedPointIndex != -1) {
      xcoords(modifiedPointIndex) = newPoint.x
      ycoords(modifiedPointIndex) = newPoint.y
    }
  }

  override def setModifiedPoint(modified: Point): Unit = {
    modifiedPointIndex = points.indexOf((modified.x, modified.y))
  }

  def moveElement(xOffset: Int, yOffset: Int) =
    for (i <- 0 until xcoords.length) {
      xcoords(i) = xcoords(i).intValue + xOffset
      ycoords(i) = ycoords(i).intValue + yOffset
    }

  def getHandles: Array[Point] =
    points.map(t => new java.awt.Point(t._1, t._2)).toArray

  def contains(p: Point): Boolean = {
    val check: AwtPolygon = new AwtPolygon(xcoords.map(_.intValue), ycoords.map(_.intValue), xcoords.length)
    check.contains(p)
  }

  override def draw(g: GraphicsInterface, turtleColor: Color, scale: Double, angle: Double) =
    if (notCompleted)
      super.draw(g, null, scale, angle)
    else {
      g.setColor(displayColor(turtleColor))
      if (filled)
        g.fillPolygon(xcoords, ycoords, xcoords.size)
      else
        g.drawPolygon(xcoords, ycoords, xcoords.size)
    }

  def finishUp() = {
    xcoords = xcoords.dropRight(3)
    ycoords = ycoords.dropRight(3)
    notCompleted = false
  }

  def selfClose() = {
    xcoords = xcoords.dropRight(1)
    ycoords = ycoords.dropRight(1)
    notCompleted = false
  }

  override def toReadableString: String =
    s"Polygon - color: $awtColor,\n          bounds: $bounds"

  override private[shape] def shouldSave: Boolean =
    xcoords.size >= 2

  override def toString: String = {
    val pointsString = points.map(p => s"${p._1} ${p._2}").mkString(" ")
    s"Polygon ${awtColor.getRGB} $filled $marked $pointsString"
  }

  var notCompleted: Boolean = false
}

