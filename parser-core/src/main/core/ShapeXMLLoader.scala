// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import Shape.{ Circle => CoreCircle, Element, Line => CoreLine, LinkLine => CoreLinkLine, LinkShape => CoreLinkShape,
               Polygon => CorePolygon, Rectangle => CoreRectangle, RgbColor, VectorShape => CoreVectorShape }

object ShapeXMLLoader {
  case class Circle(color: RgbColor, filled: Boolean, marked: Boolean, x: Int, y: Int, diameter: Int)
    extends CoreCircle

  case class Line(color: RgbColor, marked: Boolean, startPoint: (Int, Int), endPoint: (Int, Int))
    extends CoreLine {
      override def filled = false
    }

  case class Polygon(color: RgbColor, filled: Boolean, marked: Boolean, points: List[(Int, Int)]) extends CorePolygon

  case class Rectangle(color: RgbColor, filled: Boolean, marked: Boolean, upperLeftCorner: (Int, Int),
                      lowerRightCorner: (Int, Int)) extends CoreRectangle

  case class VectorShape(var name: String, rotatable: Boolean, editableColorIndex: Int, elements: List[Element])
    extends CoreVectorShape

  case class LinkLine(xcor: Double, isVisible: Boolean, dashChoices: List[Float]) extends CoreLinkLine

  case class LinkShape(var name: String, curviness: Double, linkLines: List[LinkLine], indicator: VectorShape)
    extends CoreLinkShape

  def readShape(element: XMLElement): VectorShape = {
    def colorFromString(string: String) = {
      val int = string.toInt

      RgbColor((int >> 24) & 255, (int >> 16) & 255, (int >> 8) & 255, int & 255)
    }

    val elements =
      for (element <- element.children) yield {
        element.name match {
          case "circle" =>
            Circle(colorFromString(element("color")), element("filled").toBoolean, element("marked").toBoolean,
                   element("x").toInt, element("y").toInt, element("diameter").toInt)

          case "line" =>
            Line(colorFromString(element("color")), element("marked").toBoolean,
                 (element("startX").toInt, element("startY").toInt), (element("endX").toInt, element("endY").toInt))

          case "polygon" =>
            Polygon(colorFromString(element("color")), element("filled").toBoolean, element("marked").toBoolean,
                    for (element <- element.children if element.name == "point")
                      yield (element("x").toInt, element("y").toInt))

          case "rectangle" =>
            Rectangle(colorFromString(element("color")), element("filled").toBoolean, element("marked").toBoolean,
                      (element("startX").toInt, element("startY").toInt),
                      (element("endX").toInt, element("endY").toInt))

        }
      }

    VectorShape(element("name"), element("rotatable").toBoolean, element("editableColorIndex").toInt, elements)
  }

  def writeShape(shape: CoreVectorShape): XMLElement = {
    def colorToString(color: RgbColor) =
      (color.red << 24 | color.green << 16 | color.blue << 8 | color.alpha).toString

    val attributes = Map[String, String](
      ("name", shape.name),
      ("rotatable", shape.rotatable.toString),
      ("editableColorIndex", shape.editableColorIndex.toString)
    )
    
    val children: List[XMLElement] =
      for (element <- shape.elements.toList) yield {
        element match {
          case circle: CoreCircle =>
            val attributes = Map[String, String](
              ("color", colorToString(circle.color)),
              ("filled", circle.filled.toString),
              ("marked", circle.marked.toString),
              ("x", circle.x.toString),
              ("y", circle.y.toString),
              ("diameter", circle.diameter.toString)
            )

            XMLElement("circle", attributes, "", Nil)

          case line: CoreLine =>
            val attributes = Map[String, String](
              ("color", colorToString(line.color)),
              ("marked", line.marked.toString),
              ("startX", line.startPoint._1.toString),
              ("startY", line.startPoint._2.toString),
              ("endX", line.endPoint._1.toString),
              ("endY", line.endPoint._2.toString)
            )

            XMLElement("line", attributes, "", Nil)
          
          case polygon: CorePolygon =>
            val attributes = Map[String, String](
              ("color", colorToString(polygon.color)),
              ("filled", polygon.filled.toString),
              ("marked", polygon.marked.toString)
            )

            val children =
              for (point <- polygon.points.toList) yield {
                val attributes = Map[String, String](
                  ("x", point._1.toString),
                  ("y", point._2.toString)
                )

                XMLElement("point", attributes, "", Nil)
              }

            XMLElement("polygon", attributes, "", children)

          case rectangle: CoreRectangle =>
            val attributes = Map[String, String](
              ("color", colorToString(rectangle.color)),
              ("filled", rectangle.filled.toString),
              ("marked", rectangle.marked.toString),
              ("startX", rectangle.upperLeftCorner._1.toString),
              ("startY", rectangle.upperLeftCorner._2.toString),
              ("endX", rectangle.lowerRightCorner._1.toString),
              ("endY", rectangle.lowerRightCorner._2.toString)
            )

            XMLElement("rectangle", attributes, "", Nil)

        }
      }

    XMLElement("shape", attributes, "", children)
  }

  def readLinkShape(element: XMLElement): LinkShape = {
    var lines = List[LinkLine]()
    var indicator: VectorShape = null

    for (element <- element.children) {
      element.name match {
        case "lines" =>
          for (element <- element.children if element.name == "line") {
            lines = lines :+ new LinkLine(element("x").toDouble, element("visible").toBoolean,
                                          for (element <- element.children if element.name == "dash")
                                            yield element("value").toFloat)
          }
        
        case "indicator" =>
          indicator = readShape(element.getChild("shape"))
        
        case _ =>
      }
    }

    LinkShape(element("name"), element("curviness").toDouble, lines, indicator)
  }

  def writeLinkShape(shape: CoreLinkShape): XMLElement = {
    val attributes = Map[String, String](
      ("name", shape.name),
      ("curviness", shape.curviness.toString)
    )

    val lines =
      for (line <- shape.linkLines.toList) yield {
        val attributes = Map[String, String](
          ("x", line.xcor.toString),
          ("visible", line.isVisible.toString)
        )

        val children =
          for (dash <- line.dashChoices.toList) yield {
            XMLElement("dash", Map[String, String](("value", dash.toString)), "", Nil)
          }

        XMLElement("line", attributes, "", children)
      }
    
    val children = List[XMLElement](
      XMLElement("lines", Map(), "", lines),
      XMLElement("indicator", Map(), "", List(writeShape(shape.indicator)))
    )

    XMLElement("shape", attributes, "", children)
  }
}
