// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import Shape.{
  Circle      => CoreCircle,
  Element,
  Line        => CoreLine,
  LinkLine    => CoreLinkLine,
  LinkShape   => CoreLinkShape,
  Polygon     => CorePolygon,
  Rectangle   => CoreRectangle,
  VectorShape => CoreVectorShape }

object ShapeParser {

  def parseVectorShapes(lines: Seq[String]): Seq[VectorShape]   = parseNewlineSeparatedGroups(lines, parseShape)

  def parseVectorShapes(lines: Array[String]): Seq[VectorShape] = parseNewlineSeparatedGroups(lines.toSeq, parseShape)

  def parseLinkShapes(lines: Seq[String]): Seq[LinkShape] = parseNewlineSeparatedGroups(lines, parseLink)

  def parseLinkShapes(lines: Array[String]): Seq[LinkShape] = parseNewlineSeparatedGroups(lines.toSeq, parseLink)

  def formatVectorShapes(shapes: Seq[CoreVectorShape]): String = shapes.map(formatShape).mkString("\n\n")

  def formatLinkShapes(shapes: Seq[CoreLinkShape]): String = shapes.map(formatLink).mkString("\n\n")

  private def parseNewlineSeparatedGroups[A](lines: Seq[String], f: List[String] => A): Seq[A] =
    lines.foldLeft(List[List[String]](List[String]())) {
      case (otherShapes, l: String) if l == "" => List[String]()::otherShapes
      case (currentShape::otherShapes, l: String) => (currentShape :+ l)::otherShapes
    }.foldLeft(List[List[String]]()) {
      case (l::otherShapes, Nil) => ("" +: l)::otherShapes
      case (otherShapes, l) => l::otherShapes
    }.filter(_.nonEmpty).map(f)

  private def parseShape(lines: List[String]): VectorShape =
    lines.toList match {
      case name :: rotatable :: editableColorIndex :: elements =>
        VectorShape(name, rotatable == "true", editableColorIndex.toInt, elements.map(parseElement): _*)
      case _ => throw new IllegalStateException(s"Invalid vector shape:\n ${lines.mkString("\n")}")
    }

  private def formatShape(shape: CoreVectorShape): String =
    (Seq(shape.name, shape.rotatable, shape.editableColorIndex) ++ shape.elements.map(formatElement)).mkString("\n")

  private def parseLink(lines: List[String]): LinkShape =
    lines.toList match {
      case name::curviness::ll1::ll2::ll3::indicator =>
        LinkShape(name, curviness.toDouble, Seq(ll1, ll2, ll3).map(parseLinkLine), parseShape(indicator))
      case _ =>
        throw new IllegalStateException(s"Invalid link shape: ${lines.mkString("\n")}")
    }

  private def formatLink(link: CoreLinkShape): String =
    Seq(link.name, link.curviness, link.linkLines.map(formatLinkLine).mkString("\n"), formatShape(link.indicator)).mkString("\n")

  private def parseLinkLine(line: String): LinkLine =
    line.split(' ').toList match {
      case xcor::"1"::dashChoices if CoreLinkLine.dashChoices.contains(dashChoices.map(_.toFloat).toArray) =>
        LinkLine(xcor.toDouble, true, dashChoices.map(_.toFloat))
      case xcor::"1"::dashChoices => LinkLine(xcor.toDouble, true, Seq(1.0f, 0.0f))
      case xcor::"0"::dashChoices => LinkLine(xcor.toDouble, false, Seq(0.0f, 1.0f))
      case _ => throw new IllegalStateException(s"Invalid link line: $line")
    }

  private def formatLinkLine(ll: CoreLinkLine): String = {
    val visibleString = if (ll.isVisible) "1" else "0"
    (Seq(ll.xcor, visibleString) ++ ll.dashChoices).mkString(" ")
  }

  private def parseElement(s: String): Element =
    try {
      s.split(' ').toList match {
        case "Circle"::colorString::filled::marked::x::y::diameter::Nil =>
          Circle(
            color(colorString.toInt), filled.toBoolean, marked.toBoolean,
            x.toInt, y.toInt, diameter.toInt)
        case "Line"::colorString::marked::x1::y1::x2::y2::Nil =>
          Line(color(colorString.toInt), marked.toBoolean,
            (x1.toInt, y1.toInt),
            (x2.toInt, y2.toInt))
        case "Rectangle"::colorString::filled::marked::x1::y1::x2::y2::Nil =>
          Rectangle(
            color(colorString.toInt), filled.toBoolean, marked.toBoolean,
            (x1.toInt, y1.toInt),
            (x2.toInt, y2.toInt))
        case "Polygon"::colorString::filled::marked::pointCoords =>
          // no "point" (ha ha ha) in keeping both of two consecutive
          // identical points - ST 8/1/04
          val points = pointCoords.grouped(2)
            .map(i => (i(0).toInt, i(1).toInt))
            .foldLeft(Seq[(Int, Int)]()) {
              case (s, (x, y)) if s.length > 0 && s.head == ((x, y)) => (x, y) +: s
              case (s, (x, y)) => (x, y) +: s
            }.reverse
          Polygon(color(colorString.toInt), filled.toBoolean, marked.toBoolean, points: _*)
        case _ => throw new IllegalStateException(s"Invalid shape format in file: $s")
      }
    } catch {
      case e: IllegalArgumentException =>
        throw new IllegalStateException(s"Invalid shape format in file: $s")
    }

  private def formatElement(e: Element): String =
    e match {
      case c: CoreCircle    =>
        Seq("Circle", formatColor(c.color), c.filled, c.marked, c.x, c.y, c.diameter).mkString(" ")
      case l: CoreLine      =>
        Seq("Line", formatColor(l.color), l.marked, l.startPoint._1, l.startPoint._2, l.endPoint._1, l.endPoint._2).mkString(" ")
      case p: CorePolygon   =>
        (Seq("Polygon", formatColor(p.color), p.filled, p.marked) ++
          p.points.flatMap(p => Seq(p._1, p._2))).mkString(" ")
      case r: CoreRectangle =>
        Seq("Rectangle", formatColor(r.color), r.filled, r.marked,
          r.upperLeftCorner._1, r.upperLeftCorner._2,
          r.lowerRightCorner._1, r.lowerRightCorner._2).mkString(" ")
    }

  private def formatColor(color: RgbColor): String =
   (color.alpha << 24 | color.red << 16 | color.green << 8 | color.blue).toString

  private def color(i: Int): RgbColor = {
    val (r, g, b) = ((i >> 16) & 0xFF, (i >> 8) & 0xFF, i & 0xFF)
    RgbColor(r, g, b)
  }

  case class Circle(color: RgbColor, filled: Boolean, marked: Boolean,
                    x: Int, y: Int, diameter: Int) extends Shape.Circle

  case class Line(color: RgbColor, marked: Boolean,
                  startPoint: (Int, Int), endPoint: (Int, Int)) extends Shape.Line {
    override def filled: Boolean = false
  }

  case class Rectangle(color: RgbColor, filled: Boolean, marked: Boolean,
                       upperLeftCorner: (Int, Int), lowerRightCorner: (Int, Int)) extends Shape.Rectangle
  case class Polygon(color: RgbColor, filled: Boolean,
                     marked: Boolean, points: (Int, Int)*) extends Shape.Polygon

  case class LinkLine(xcor: Double, isVisible: Boolean, dashChoices: Seq[Float]) extends Shape.LinkLine

  case class VectorShape(var name: String, rotatable: Boolean, editableColorIndex: Int, elements: Element*) extends Shape.VectorShape
  case class LinkShape(var name: String, curviness: Double, linkLines: Seq[LinkLine], indicator: VectorShape) extends Shape.LinkShape
}
