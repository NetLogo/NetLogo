// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo
package org.nlogo.shape

import org.nlogo.api.GraphicsInterface
import java.awt.geom.AffineTransform
import java.awt.geom.QuadCurve2D
import java.awt.{ Shape => JShape }
import java.util.StringTokenizer
import java.awt.Color
import org.nlogo.core.Shape.{ LinkLine => BaseLinkLine }

@SerialVersionUID(0L)
object LinkLine {
  val dashChoices: Array[Array[Float]] =
    Array(Array(0.0f, 1.0f),
          Array(1.0f, 0.0f),
          Array(2.0f, 2.0f),
          Array(4.0f, 4.0f),
          Array(4.0f, 4.0f, 2.0f, 2.0f))
}

@SerialVersionUID(0L)
class LinkLine(var xcor: Double = 0, var visible: Boolean = false, var dashes:Array[Float] = LinkLine.dashChoices(1))
  extends BaseLinkLine
  with java.io.Serializable
  with Cloneable {

  override def dashChoices: Seq[Float] = dashes.toSeq

  override def isVisible: Boolean = visible

  def this() = this(0, false, LinkLine.dashChoices(1))

  def this(xcor: Double, isVisible: Boolean) = {
    this(xcor, isVisible, dashes = LinkLine.dashChoices(if (isVisible) 1 else 0))
  }

  def isStraightPlainLine: Boolean =
    visible && (xcor == 0) && (dashes.length == 2) && (dashes(0) == 1) && (dashes(1) == 0)

  def paint(g: GraphicsInterface, color: Color, cellSize: Double, strokeWidth: Float, shape: JShape) = {
    g.setColor(color)
    g.setStroke(strokeWidth, dashes)
    g.draw(shape)
  }

  def getShape(x1: Double, y1: Double,
               x2: Double, y2: Double,
               curviness: Double, size: Double, cellSize: Double, stroke: Float): JShape = {
    val ycomp: Double = (x1 - x2) / size
    val xcomp: Double = (y2 - y1) / size
    val trans: AffineTransform = AffineTransform.getTranslateInstance(xcomp * xcor * stroke, ycomp * xcor * stroke)
    val midX: Double = ((x1 + x2) / 2) + (curviness * xcomp)
    val midY: Double = ((y1 + y2) / 2) + (curviness * ycomp)
    trans.createTransformedShape(new QuadCurve2D.Double(x1, y1, midX, midY, x2, y2))
  }

  def parseDashes(str: String) = {
    val tokenizer: StringTokenizer = new StringTokenizer(str)
    dashes = (for (i <- 0 until tokenizer.countTokens()) yield tokenizer.nextToken.toFloat).toArray
  }

  def dashinessString: String = dashes.mkString(" ")

  override def clone: AnyRef =
    try {
      val line: LinkLine = super.clone.asInstanceOf[LinkLine]
      line.dashes = dashes
      line
    } catch {
      case ex: CloneNotSupportedException => throw new IllegalStateException(ex)
    }

  override def toString: String =
    s"$xcor ${if (visible) "1" else "0"} $dashinessString"

  def toReadableString: String =
    s"Link Line with xcor = $xcor $visible $dashinessString"
}
