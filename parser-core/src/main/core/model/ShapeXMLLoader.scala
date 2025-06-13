// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core.model

import org.nlogo.core.Shape.{ Circle => CoreCircle, Element, Line => CoreLine, LinkLine => CoreLinkLine
                              , LinkShape => CoreLinkShape, Polygon => CorePolygon, Rectangle => CoreRectangle
                              , RgbColor, VectorShape => CoreVectorShape }

object ShapeXMLLoader {

  case class Circle(color: RgbColor, filled: Boolean, marked: Boolean, x: Int, y: Int, diameter: Int)
    extends CoreCircle

  case class Line(color: RgbColor, marked: Boolean, startPoint: (Int, Int), endPoint: (Int, Int)) extends CoreLine {
    override def filled = false
  }

  case class Polygon(color: RgbColor, filled: Boolean, marked: Boolean, points: Seq[(Int, Int)]) extends CorePolygon

  case class Rectangle( color: RgbColor, filled: Boolean, marked: Boolean, upperLeftCorner: (Int, Int)
                      , lowerRightCorner: (Int, Int)) extends CoreRectangle

  case class VectorShape(var name: String, rotatable: Boolean, editableColorIndex: Int, elements: Seq[Element])
    extends CoreVectorShape

  case class LinkLine(xcor: Double, isVisible: Boolean, dashChoices: Seq[Float]) extends CoreLinkLine

  case class LinkShape(var name: String, curviness: Double, linkLines: Seq[LinkLine], indicator: VectorShape)
    extends CoreLinkShape

  def readShape(element: XMLElement): VectorShape = {

    def colorFromString(string: String) = {
      val int = string.toInt
      RgbColor((int >> 24) & 255, (int >> 16) & 255, (int >> 8) & 255, int & 255)
    }

    val elements: Seq[Element] =
      element.children.collect {
        case el @ XMLElement("circle", _, _, _) =>
          Circle( colorFromString(el("color")), el("filled").toBoolean, el("marked").toBoolean
                , el("x").toInt, el("y").toInt, el("diameter").toInt)

        case el @ XMLElement("line", _, _, _) =>
          Line( colorFromString(el("color")), el("marked").toBoolean, (el("startX").toInt
              , el("startY").toInt), (el("endX").toInt, el("endY").toInt))

        case el @ XMLElement("polygon", _, _, _) =>
          Polygon( colorFromString(el("color")), el("filled").toBoolean, el("marked").toBoolean
                 , el.getChildren("point").map(point => (point("x").toInt, point("y").toInt)).toList)

        case el @ XMLElement("rectangle", _, _, _) =>
          Rectangle( colorFromString(el("color")), el("filled").toBoolean, el("marked").toBoolean
                   , (el("startX").toInt, el("startY").toInt)
                   , (el("endX").toInt, el("endY").toInt))

        // ignore other shapes for compatibility with other versions in the future (Isaac B 2/12/25)

      }

    VectorShape(element("name"), element("rotatable").toBoolean, element("editableColorIndex").toInt, elements)

  }

  def writeShape(shape: CoreVectorShape): XMLElement = {

    def colorToString(color: RgbColor) =
      (color.red << 24 | color.green << 16 | color.blue << 8 | color.alpha).toString

    val attributes =
      Map( "name"               -> shape.name
         , "rotatable"          -> shape.rotatable.toString
         , "editableColorIndex" -> shape.editableColorIndex.toString
         )

    val children: Seq[XMLElement] =
      shape.elements.collect {

        case circle: CoreCircle =>
          val attributes =
            Map( "color"    -> colorToString(circle.color)
                , "filled"   -> circle.filled.toString
                , "marked"   -> circle.marked.toString
                , "x"        -> circle.x.toString
                , "y"        -> circle.y.toString
                , "diameter" -> circle.diameter.toString
                )
          XMLElement("circle", attributes, "", Seq())

        case line: CoreLine =>
          val attributes =
            Map( "color"  -> colorToString(line.color)
                , "marked" -> line.marked.toString
                , "startX" -> line.startPoint._1.toString
                , "startY" -> line.startPoint._2.toString
                , "endX"   -> line.endPoint._1.toString
                , "endY"   -> line.endPoint._2.toString
                )
          XMLElement("line", attributes, "", Seq())

        case polygon: CorePolygon =>
          val attributes =
            Map( "color"  -> colorToString(polygon.color)
                , "filled" -> polygon.filled.toString
                , "marked" -> polygon.marked.toString
                )

          val children =
            polygon.points.map {
              case (x, y) => XMLElement("point", Map("x" -> x.toString, "y" -> y.toString), "", Seq())
            }

          XMLElement("polygon", attributes, "", children)

        case rectangle: CoreRectangle =>
          val attributes =
            Map( "color"  -> colorToString(rectangle.color)
                , "filled" -> rectangle.filled.toString
                , "marked" -> rectangle.marked.toString
                , "startX" -> rectangle.upperLeftCorner._1.toString
                , "startY" -> rectangle.upperLeftCorner._2.toString
                , "endX"   -> rectangle.lowerRightCorner._1.toString
                , "endY"   -> rectangle.lowerRightCorner._2.toString
                )
          XMLElement("rectangle", attributes, "", Seq())

        // ignore other sections for compatibility with other versions in the future (Isaac B 2/12/25)
      }

    XMLElement("shape", attributes, "", children)

  }

  def readLinkShape(element: XMLElement): LinkShape = {

    val (lines, indicatorOpt) =
      element.children.foldLeft((Seq[LinkLine](), Option.empty[VectorShape])) {

        case ((ls, indic), el @ XMLElement("lines", _, _, _)) =>
          val xs =
            el.getChildren("line").map {
              e => new LinkLine(e("x").toDouble, e("visible").toBoolean, e.getChildren("dash").map(_("value").toFloat))
            }
          (xs, indic)

        case ((ls, indic), el @ XMLElement("indicator", _, _, _)) =>
          (ls, Option(readShape(el.getChild("shape"))))

        case (          _,      XMLElement(otherName, _, _, _)) =>
          throw new Exception(s"Unexpected link shape XML value: ${otherName}")

      }

    val indicator =
      indicatorOpt.getOrElse(
        throw new Exception(s"Link shape '${element.name}' was deserialized without the required 'indicator' value")
      )

    LinkShape(element("name"), element("curviness").toDouble, lines, indicator)

  }

  def writeLinkShape(shape: CoreLinkShape): XMLElement = {

    val attributes =
      Map( "name"      -> shape.name
         , "curviness" -> shape.curviness.toString
         )

    val lines =
      shape.linkLines.map(
        (line) => {
          val attributes =
            Map( "x"       -> line.xcor.toString
               , "visible" -> line.isVisible.toString
               )
          val children = line.dashChoices.map((d) => XMLElement("dash", Map("value" -> d.toString), "", Seq()))
          XMLElement("line", attributes, "", children)
        }
      )

    val children =
      Seq( XMLElement("lines"    , Map(), "", lines)
         , XMLElement("indicator", Map(), "", Seq(writeShape(shape.indicator)))
         )

    XMLElement("shape", attributes, "", children)

  }

}
