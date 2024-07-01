// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import javax.xml.stream.XMLStreamWriter

import Shape.{ Circle => CoreCircle, Element, Line => CoreLine, LinkLine => CoreLinkLine, LinkShape => CoreLinkShape,
               Polygon => CorePolygon, Rectangle => CoreRectangle, RgbColor, VectorShape => CoreVectorShape }

case class Circle(color: RgbColor, filled: Boolean, marked: Boolean, x: Int, y: Int, diameter: Int) extends CoreCircle

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

object ShapeXMLLoader {
  def readShape(element: XMLElement): VectorShape = {
    def colorFromString(string: String) = {
      val int = string.toInt

      RgbColor((int >> 24) & 255, (int >> 16) & 255, (int >> 8) & 255, int & 255)
    }

    val elements =
      for (element <- element.children) yield {
        element.name match {
          case "circle" =>
            Circle(colorFromString(element.attributes("color")), element.attributes("filled").toBoolean,
                   element.attributes("marked").toBoolean, element.attributes("x").toInt,
                   element.attributes("y").toInt, element.attributes("diameter").toInt)

          case "line" =>
            Line(colorFromString(element.attributes("color")), element.attributes("marked").toBoolean,
                 (element.attributes("startX").toInt, element.attributes("startY").toInt),
                 (element.attributes("endX").toInt, element.attributes("endY").toInt))

          case "polygon" =>
            Polygon(colorFromString(element.attributes("color")),
                        element.attributes("filled").toBoolean, element.attributes("marked").toBoolean,
                        for (element <- element.children if element.name == "point")
                          yield (element.attributes("x").toInt, element.attributes("y").toInt))

          case "rectangle" =>
            Rectangle(colorFromString(element.attributes("color")),
                          element.attributes("filled").toBoolean, element.attributes("marked").toBoolean,
                          (element.attributes("startX").toInt, element.attributes("startY").toInt),
                          (element.attributes("endX").toInt, element.attributes("endY").toInt))
          
          case _ => null
        }
      }

    VectorShape(element.attributes("name"), element.attributes("rotatable").toBoolean,
                element.attributes("editableColorIndex").toInt, elements)
  }

  def writeShape(writer: XMLStreamWriter, shape: CoreVectorShape) {
    def colorToString(color: RgbColor) =
      (color.red << 24 | color.green << 16 | color.blue << 8 | color.alpha).toString

    writer.writeStartElement("shape")

    writer.writeAttribute("name", shape.name)
    writer.writeAttribute("rotatable", shape.rotatable.toString)
    writer.writeAttribute("editableColorIndex", shape.editableColorIndex.toString)
    
    for (element <- shape.elements) {
      element match {
        case circle: CoreCircle =>
          writer.writeStartElement("circle")

          writer.writeAttribute("color", colorToString(circle.color))
          writer.writeAttribute("filled", circle.filled.toString)
          writer.writeAttribute("marked", circle.marked.toString)
          writer.writeAttribute("x", circle.x.toString)
          writer.writeAttribute("y", circle.y.toString)
          writer.writeAttribute("diameter", circle.diameter.toString)

          writer.writeEndElement

        case line: CoreLine =>
          writer.writeStartElement("line")

          writer.writeAttribute("color", colorToString(line.color))
          writer.writeAttribute("marked", line.marked.toString)
          writer.writeAttribute("startX", line.startPoint._1.toString)
          writer.writeAttribute("startY", line.startPoint._2.toString)
          writer.writeAttribute("endX", line.endPoint._1.toString)
          writer.writeAttribute("endY", line.endPoint._2.toString)

          writer.writeEndElement
        
        case polygon: CorePolygon =>
          writer.writeStartElement("polygon")

          writer.writeAttribute("color", colorToString(polygon.color))
          writer.writeAttribute("filled", polygon.filled.toString)
          writer.writeAttribute("marked", polygon.marked.toString)

          for (point <- polygon.points) {
            writer.writeStartElement("point")

            writer.writeAttribute("x", point._1.toString)
            writer.writeAttribute("y", point._2.toString)

            writer.writeEndElement
          }

          writer.writeEndElement

        case rectangle: CoreRectangle =>
          writer.writeStartElement("rectangle")

          writer.writeAttribute("color", colorToString(rectangle.color))
          writer.writeAttribute("filled", rectangle.filled.toString)
          writer.writeAttribute("marked", rectangle.marked.toString)
          writer.writeAttribute("startX", rectangle.upperLeftCorner._1.toString)
          writer.writeAttribute("startY", rectangle.upperLeftCorner._2.toString)
          writer.writeAttribute("endX", rectangle.lowerRightCorner._1.toString)
          writer.writeAttribute("endY", rectangle.lowerRightCorner._2.toString)

          writer.writeEndElement
        
        case _ =>
      }
    }

    writer.writeEndElement
  }

  def readLinkShape(element: XMLElement): LinkShape = {
    var lines = List[LinkLine]()
    var indicator: VectorShape = null

    for (element <- element.children) {
      element.name match {
        case "lines" =>
          for (element <- element.children if element.name == "line") {
            lines = lines :+ new LinkLine(element.attributes("x").toInt, element.attributes("visible").toBoolean,
                                          for (element <- element.children if element.name == "dash")
                                            yield element.attributes("value").toFloat)
          }
        
        case "indicator" =>
          indicator = readShape(element)
        
        case _ =>
      }
    }

    LinkShape(element.attributes("name"), element.attributes("curviness").toDouble, lines, indicator)
  }

  def writeLinkShape(writer: XMLStreamWriter, shape: CoreLinkShape) = {
    writer.writeStartElement("shape")

    writer.writeAttribute("name", shape.name)
    writer.writeAttribute("curviness", shape.curviness.toString)
    
    writer.writeStartElement("lines")

    for (line <- shape.linkLines) {
      writer.writeStartElement("line")

      writer.writeAttribute("x", line.xcor.toString)
      writer.writeAttribute("visible", line.isVisible.toString)

      for (dash <- line.dashChoices) {
        writer.writeStartElement("dash")
        writer.writeAttribute("value", dash.toString)
        writer.writeEndElement
      }

      writer.writeEndElement
    }

    writer.writeEndElement

    writer.writeStartElement("indicator")

    writeShape(writer, shape.indicator)

    writer.writeEndElement

    writer.writeEndElement
  }
}
