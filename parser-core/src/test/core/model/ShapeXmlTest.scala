// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core.model

import
  org.nlogo.core.{ RgbColor, Shape },
    Shape.{ LinkShape, VectorShape => CoreVectorShape }

import
  org.nlogo.xmllib.{ DummyXml, Element, XmlReader },
    DummyXml._

import
  org.scalatest.{ FunSuite, Matchers },
    Matchers._

object ShapeXmlTest {
  val emptyTurtleXml = Elem("vectorShape",
    Seq(Attr("name", "default"), Attr("rotatable", "true"), Attr("editableColorIndex", "0")),
    Seq(Elem("elements", Seq(), Seq())))

  val emptyTurtleShape = VectorShape("default", true, 0, Nil)

  val circle = CircleElem(RgbColor(1, 2, 3), true, false, 5, 10, 20)
  val rectangle = RectangleElem(RgbColor(1, 2, 3), true, false, 5, 10, 20, 40)
  val polygon = PolygonElem(RgbColor(1, 2, 3), true, false, Seq((1, 2), (4, 8), (16, 32)))
  val line = LineElem(RgbColor(1, 2, 3), false, false, 5, 10, 20, 40)

  val circleXml = Elem("circle",
    Seq(Attr("cx", "5"), Attr("cy", "10"), Attr("diameter", "20"),
      Attr("color", "#010203"), Attr("filled", "true"), Attr("marked", "false")),
    Seq())
  val lineXml = Elem("line",
    Seq(Attr("x1", "5"), Attr("y1", "10"), Attr("x2", "20"), Attr("y2", "40"),
      Attr("color", "#010203"), Attr("filled", "false"), Attr("marked", "false")),
    Seq())
  val polygonXml = Elem("polygon",
    Seq(Attr("points", "1,2 4,8 16,32"),
      Attr("color", "#010203"), Attr("filled", "true"), Attr("marked", "false")),
    Seq())
  val rectangleXml = Elem("rect",
    Seq(Attr("x", "5"), Attr("y", "10"), Attr("width", "20"), Attr("height", "40"),
      Attr("color", "#010203"), Attr("filled", "true"), Attr("marked", "false")),
    Seq())

  val circleTurtle = shapeWith(circle)
  val circleTurtleXml = xmlWith(circleXml)
  val lineTurtle = shapeWith(line)
  val lineTurtleXml = xmlWith(lineXml)
  val polyTurtle = shapeWith(polygon)
  val polyTurtleXml = xmlWith(polygonXml)
  val rectTurtle = shapeWith(rectangle)
  val rectTurtleXml = xmlWith(rectangleXml)

  val multiShapeTurtle =
    emptyTurtleShape.copy(elements = List(circle, line, polygon, rectangle))

  val multiShapeTurtleXml =
    emptyTurtleXml.copy(children = Seq(Elem("elements", Seq(), Seq(circleXml, lineXml, polygonXml, rectangleXml))))

  val otherLineTurtle = {
    import org.nlogo.core.ShapeParser.{ VectorShape => OtherVectorShape }
    import org.nlogo.core.ShapeParser._
    OtherVectorShape("default", true, 0, Line(RgbColor(1, 2, 3), false, (5, 10), (20, 40)))
  }

  // offset is typically one of { -0.2, 0.0, 0.2 }
  def linkLineXml(offset: Double): Elem =
    Elem("line",
      Seq(Attr("offset", XmlReader.formatDouble(offset)), Attr("isVisible", "true"), Attr("stroke-dasharray", "1.0,0.0")),
      Seq())

  def linkLine(offset: Double): ParsedLinkLine =
    ParsedLinkLine(offset, true, Seq(1.0f, 0.0f))

  val linkShapeXml =
    Elem("linkShape",
      Seq(Attr("name", "foo"), Attr("curviness", "0.0")),
      Seq(linkLineXml(-0.2), linkLineXml(0.0), linkLineXml(0.2), rectTurtleXml.copy(tag = "indicator")))

  val linkShape =
    ParsedLinkShape("foo", 0, Seq(linkLine(-0.2), linkLine(0.0), linkLine(0.2)), rectTurtle)

  def shapeWith(e: XmlShapeElement, t: VectorShape = emptyTurtleShape): VectorShape =
    t.copy(elements = t.elements :+ e)

  def xmlWith(e: Elem): Elem =
    emptyTurtleXml.copy(children = Seq(Elem("elements", Seq(), Seq(e))))
}

class ShapeXmlTest extends FunSuite with XmlEquality {
  import ShapeXmlTest._

  def readTurtleFromXml(x: Elem): CoreVectorShape =
    VectorShapeXml.read(x).toOption.get

  def readLinkFromXml(x: Elem): LinkShape =
    LinkShapeXml.read(x).toOption.get

  def writeTurtleToXml(s: CoreVectorShape): Element =
    VectorShapeXml.write(XmlShape.coerceVectorShape(s), Factory)

  def writeLinkToXml(s: LinkShape): Element =
    LinkShapeXml.write(XmlShape.coerceLinkShape(s), Factory)

  test("reads empty turtle shape") {
    assertResult(emptyTurtleShape)(readTurtleFromXml(emptyTurtleXml))
  }

  test("writes empty turtle shape") {
    writeTurtleToXml(emptyTurtleShape) should beXmlEqualTo (emptyTurtleXml)
  }

  test("reads turtle shape with circle") {
    assertResult(circleTurtle)(readTurtleFromXml(circleTurtleXml))
  }

  test("writes turtle shape with circle") {
    writeTurtleToXml(circleTurtle) should beXmlEqualTo (circleTurtleXml)
  }

  test("reads turtle shape with rectangle") {
    assertResult(rectTurtle)(readTurtleFromXml(rectTurtleXml))
  }

  test("writes turtle shape with rectangle") {
    writeTurtleToXml(rectTurtle) should beXmlEqualTo (rectTurtleXml)
  }

  test("reads turtle shape with line") {
    assertResult(lineTurtle)(readTurtleFromXml(lineTurtleXml))
  }

  test("writes turtle shape with line") {
    writeTurtleToXml(lineTurtle) should beXmlEqualTo (lineTurtleXml)
  }

  test("reads turtle shape with polygon") {
    assertResult(polyTurtle)(readTurtleFromXml(polyTurtleXml))
  }

  test("writes turtle shape with polygon") {
    writeTurtleToXml(polyTurtle) should beXmlEqualTo (polyTurtleXml)
  }

  test("reads turtle shape with multiple shapes") {
    assertResult(multiShapeTurtle)(readTurtleFromXml(multiShapeTurtleXml))
  }

  test("writes turtle shape with multiple shapes") {
    writeTurtleToXml(multiShapeTurtle) should beXmlEqualTo (multiShapeTurtleXml)
  }

  test("reads link shapes") {
    assertResult(linkShape)(readLinkFromXml(linkShapeXml))
  }

  test("writes link shapes") {
    writeLinkToXml(linkShape) should beXmlEqualTo (linkShapeXml)
  }

  test("writes turtle shape for other types of TurtleShapes") {
    writeTurtleToXml(otherLineTurtle) should beXmlEqualTo (lineTurtleXml)
  }
}
