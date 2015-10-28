// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape

import java.awt.{Point, Color}

import org.nlogo.core.{ Shape => CoreShape },
  CoreShape.{ RgbColor, Circle => BaseCircle, Element => BaseElement,
  Line => BaseLine, LinkLine => BaseLinkLine, LinkShape => BaseLinkShape, Polygon => BasePolygon,
  Rectangle => BaseRectangle, VectorShape => BaseVectorShape }

object ShapeConverter {
  def baseShapeToShape(bs: CoreShape): CoreShape =
    bs match {
      case vs: BaseVectorShape => baseVectorShapeToVectorShape(vs)
      case ls: BaseLinkShape => baseLinkShapeToLinkShape(ls)
    }

  def baseLinkShapeToLinkShape(l: BaseLinkShape): LinkShape = {
    import l._
    val ls = new LinkShape()
    ls.name = name
    ls.curviness = curviness
    ls.directionIndicator = baseVectorShapeToVectorShape(indicator)
    ls
  }

  def baseVectorShapeToVectorShape(v: BaseVectorShape): VectorShape = {
    import v._
    val vs = new VectorShape()
    vs.name = name
    vs.setRotatable(rotatable)
    vs.setEditableColorIndex(editableColorIndex)
    elements.map(e => vs.add(coreElementToElement(e)))
    vs
  }

  def color(c: RgbColor): Color =
    new Color(c.red, c.green, c.blue, c.alpha)

  def point(t: (Int, Int)): Point =
    new Point(t._1, t._2)

  def coreElementToElement(e: BaseElement): Element =
    e match {
      case bl: BaseLine =>
        val line = new Line(point(bl.startPoint), point(bl.endPoint), color(bl.color))
        line.marked = bl.marked
        line
      case bp: BasePolygon =>
        val poly = new Polygon(bp.xCoords.toList, bp.yCoords.toList, color(bp.color))
        poly.filled = bp.filled
        poly.marked = bp.marked
        poly
      case bc: BaseCircle =>
        val circle = new Circle(bc.x, bc.y, bc.diameter, color(bc.color))
        circle.filled = bc.filled
        circle.marked = bc.marked
        circle
      case br: BaseRectangle =>
        val rect = new Rectangle(point(br.upperLeftCorner), point(br.lowerRightCorner), color(br.color))
        rect.filled = br.filled
        rect.marked = br.marked
        rect
    }

  def coreLinkLineToLinkLine(l: BaseLinkLine): LinkLine = {
    val ll = new LinkLine(l.xcor, l.isVisible)
    ll.dashes = l.dashChoices.toArray
    ll
  }
}
