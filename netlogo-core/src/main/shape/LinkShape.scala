// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo
package org.nlogo.shape

import org.nlogo.api.GraphicsInterface
import java.awt.{ Shape => JShape, Color }
import java.awt.geom.AffineTransform
import java.awt.geom.Line2D
import java.awt.geom.PathIterator
import java.awt.geom.Point2D
import java.awt.geom.Ellipse2D
import java.awt.Point
import org.nlogo.core.{ShapeParser, Shape}, Shape.{ LinkShape => BaseLinkShape }
import org.nlogo.api.Link

@SerialVersionUID(0L)
object LinkShape {
  def getDefaultLinkShape: LinkShape = {
    val result: LinkShape = new LinkShape
    result.name = "default"
    result.directionIndicator = getDefaultLinkDirectionShape
    result
  }

  def getDefaultLinkDirectionShape: VectorShape = {
    val result: VectorShape = new VectorShape
    result.name = "link direction"
    result.setRotatable(true)
    result.setEditableColorIndex(0)
    val l1 = new Line(new Point(150, 150), new Point(90, 180), new Color(141, 141, 141))
    l1.marked = true
    result.addElement(l1)
    val l2 = new Line(new Point(150, 150), new Point(210, 180), new Color(141, 141, 141))
    l2.marked = true
    result.addElement(l2)
    result
  }
}

@SerialVersionUID(0L)
class LinkShape extends BaseLinkShape with Cloneable with java.io.Serializable with DrawableShape {
  var name: String = ""
  val isRotatable = true
  val getEditableColorIndex = 0

  var curviness: Double = .0

  override def linkLines: Seq[Shape.LinkLine] =
    lines.toSeq

  def indicator: Shape.VectorShape = directionIndicator

  var directionIndicator: VectorShape = LinkShape.getDefaultLinkDirectionShape

  private var lines: Array[LinkLine] = Array[LinkLine](
    new LinkLine(-0.2, false),
    new LinkLine(0.0, true),
    new LinkLine(0.2, false))

  def getLine(i: Int): LinkLine = lines(i)

  def setLineVisible(index: Int, visible: Boolean) =
    lines(index).visible = visible

  def add(index: Int, line: LinkLine) =
    lines(index) = line

  def setDashiness(index: Int, dashes: Array[Float]) =
    lines(index).dashes = dashes

  def dashinessString(index: Int): String =
    lines(index).dashinessString

  def setDashes(index: Int, str: String) =
    lines(index).parseDashes(str)

  def isTooSimpleToPaint: Boolean =
    !lines(0).visible && !lines(2).visible && curviness == 0 && lines(1).isStraightPlainLine

  def paint(g: GraphicsInterface, color: Color, x: Int, y: Int, cellSize: Double, angle: Int) =
    paint(g, color, x, y, cellSize / 2, 2, angle, 0, 0, true)

  //scalastyle:off parameter.number
  def paint(g: GraphicsInterface, color: Color,
            x: Double, y: Double,
            cellSize: Double, size: Double,
            angle: Int, lineThickness: Double,
            destSize: Double, isDirected: Boolean): Unit = {
    val aR: Double = StrictMath.toRadians(angle)
    val aSin: Double = StrictMath.sin(aR) * size * cellSize
    val aCos: Double = StrictMath.cos(aR) * size * cellSize
    paint(g, color, x + aSin + (cellSize * size / 2), y + aCos, x + (cellSize * size / 2), y, cellSize, size, lineThickness, destSize, isDirected)
  }

  def paint(g: GraphicsInterface, color: Color,
            x1: Double, y1: Double,
            x2: Double, y2: Double,
            cellSize: Double, size: Double,
            lineThickness: Double, destSize: Double,
            isDirected: Boolean) = {
    for (line <- lines if line.visible)  {
      val lt: Float = StrictMath.max(1, cellSize * lineThickness).toFloat
      val shape: JShape = line.getShape(x1, y1, x2, y2, curviness, size, cellSize, lt)
      line.paint(g, color, cellSize, lt, shape)
    }
    if (isDirected) {
      val arc: JShape = lines(1).getShape(x2, y2, x1, y1, -curviness, size, cellSize, 1)
      paintDirectionIndicator(g, color, arc, cellSize, lineThickness, size, destSize + 1)
    }
  }

  def directionIndicatorTransform(x1: Double, y1: Double,
    x2: Double, y2: Double,
    linkLength: Double, destSize: Double, link: Link,
    cellSize: Double, size: Double): Array[Double] =
    if (curviness == 0)
      Array[Double](
        link.heading,
        x2 + ((x1 - x2) / linkLength * destSize * 2 / 3),
        y2 - ((y2 - y1) / linkLength * destSize * 2 / 3))
    else {
      val arc: JShape = lines(1).getShape(x2, y2, x1, y1, curviness * 3, linkLength, cellSize, 1)
      val trans: Array[Double] = directionIndicatorTransform(arc, destShape(arc, destSize, cellSize))
      trans(0) = -trans(0) + 180
      trans
    }

  def paintDirectionIndicator(g: GraphicsInterface, color: Color,
                              x1: Double, y1: Double,
                              x2: Double, y2: Double,
                              heading: Double, cellSize: Double,
                              lineThickness: Double, destSize: Double, linkLength: Double) = {
    var xcomp: Double = (x1 - x2) / linkLength * destSize * 2 / 3
    var ycomp: Double = (y2 - y1) / linkLength * destSize * 2 / 3
    val xmid: Double = (x1 - x2) / 2
    val ymid: Double = (y2 - y1) / 2
    if (StrictMath.abs(xmid) < StrictMath.abs(xcomp) && StrictMath.abs(ymid) < StrictMath.abs(ycomp)) {
      xcomp = xmid
      ycomp = ymid
    }
    val scaleFactor: Double = directionIndicatorScale(lineThickness, cellSize)
    directionIndicator.paint(g, color,
      x2 + xcomp - (cellSize * scaleFactor / 2),
      y2 - ycomp - (cellSize * scaleFactor / 2),
      scaleFactor, cellSize, heading.toInt, lineThickness)
  }
  //scalastyle:on parameter.number

  def numLines: Int = lines.count(_.visible)

  def paintDirectionIndicator(g: GraphicsInterface, color: Color,
                              arc: JShape, cellSize: Double,
                              lineThickness: Double, size: Double, destSize: Double) = {
    val trans: Array[Double] = directionIndicatorTransform(arc, destShape(arc, destSize, cellSize))
    val scale: Double = directionIndicatorScale(lineThickness, cellSize)
    directionIndicator.paint(g, color, trans(1) - (cellSize * scale / 2), trans(2) - (cellSize * scale / 2), scale, cellSize, trans(0).toInt, lineThickness)
  }

  private def directionIndicatorScale(lineThickness: Double, cellSize: Double): Double =
    (lineThickness * StrictMath.sqrt(cellSize / 2) + 2) * StrictMath.max(1, numLines / 1.5)

  def destShape(arc: JShape, size: Double, cellSize: Double): JShape = {
    val i: PathIterator = arc.getPathIterator(null, 1)
    val p: Array[Double] = new Array[Double](6)
    i.currentSegment(p)
    val trans: AffineTransform = AffineTransform.getTranslateInstance(p(0), p(1))
    trans.scale(cellSize, cellSize)
    trans.scale(size, size)
    trans.createTransformedShape(new Ellipse2D.Double(-0.5, -0.5, 1, 1))
  }

  def directionIndicatorTransform(arc: JShape, dest: JShape): Array[Double] = {
    val pts: Array[Double] = new Array[Double](6)
    var p1: Point2D = null
    var p2: Point2D = null
    val i: PathIterator = arc.getPathIterator(null, 1)
    while (!i.isDone) {
      {
        val ret: Int = i.currentSegment(pts)
        if (ret == PathIterator.SEG_MOVETO) {
          p2 = new Point2D.Double(pts(0), pts(1))
        }
        else if (ret == PathIterator.SEG_LINETO) {
          p1 = p2
          p2 = new Point2D.Double(pts(0), pts(1))
          if (dest.contains(p1) && !dest.contains(p2)) {
            return directionIndicatorTransform(new Line2D.Double(p1, p2), dest)
          }
        }
      }
      i.next()
    }
    new Array[Double](3)
  }

  def directionIndicatorTransform(initialLine: Line2D, dest: JShape): Array[Double] = {
    var line = initialLine
    var dx: Double = line.getX1 - line.getX2
    var dy: Double = line.getY1 - line.getY2
    while ((dx * dx + dy * dy) > 1) {
      line = lastOutsideSegment(line, dest)
      dx = line.getX1 - line.getX2
      dy = line.getY1 - line.getY2
    }
    val angle: Double = (270 + StrictMath.toDegrees(StrictMath.PI + StrictMath.atan2(dy, dx))) % 360
    Array[Double](angle, line.getX1, line.getY1)
  }

  protected def lastOutsideSegment(initialLine: Line2D, dest: JShape): Line2D = {
    var line = initialLine
    val left: Line2D = new Line2D.Double
    val right: Line2D = new Line2D.Double
    do {
      split(line, left, right)
      line = left
    } while (!dest.contains(line.getP2))
    right
  }

  protected def split(src: Line2D, left: Line2D, right: Line2D) = {
    val x1: Double = src.getX1
    val y1: Double = src.getY1
    val x2: Double = src.getX2
    val y2: Double = src.getY2
    val mx: Double = x1 + (x2 - x1) / 2.0
    val my: Double = y1 + (y2 - y1) / 2.0
    left.setLine(x1, y1, mx, my)
    right.setLine(mx, my, x2, y2)
  }

  override def clone: AnyRef =
    try {
      val newShape = super.clone.asInstanceOf[LinkShape]
      newShape.directionIndicator = directionIndicator.clone.asInstanceOf[VectorShape]
      newShape.lines = new Array[LinkLine](lines.length)
      for (i <- 0 until lines.length) {
        newShape.lines(i) = lines(i).clone.asInstanceOf[LinkLine]
      }
      newShape
    } catch {
      case ex: CloneNotSupportedException => throw new IllegalStateException(ex)
    }

  override def toString: String = {
    val linesString = lines.map(_.toString).mkString("\n")
    s"""|$name
        |$curviness
        |$linesString
        |${directionIndicator.toString}""".stripMargin
  }
}
