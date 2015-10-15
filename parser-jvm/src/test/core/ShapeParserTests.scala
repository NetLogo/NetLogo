// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import org.scalatest.FunSuite

class ShapeParserTests extends FunSuite {
  import Shape.{ Element, RgbColor }
  import ShapeParser._

  def assertParsesTo(shapeText: Seq[String], shapes: VectorShape*) =
    assertResult(shapes.toSeq)(ShapeParser.parseVectorShapes(shapeText))

  def assertFormatsTo(shapeText: Seq[String], shapes: VectorShape*) =
    assertResult(shapeText.mkString("\n"))(ShapeParser.formatVectorShapes(shapes))

  def vectorShapeString(name: String, rotatable: String,
    editableColorIndex: String, elements: String*): Seq[String] =
    Seq(name, rotatable, editableColorIndex) ++ elements

  def linkShapeString(name: String = "default",
                    linkLines: Seq[String] = Seq("-0.2 1 0.0 1.0", "0.0 1 1.0 0.0", "0.2 1 0.0 1.0" )): Seq[String] =
    Seq(name, "0.0") ++ linkLines ++ Seq("link direction", "true", "0",
        "Line -7500403 true 150 150 90 180", "Line -7500403 true 150 150 210 180")

  val defaultLinkLines =
    Seq(LinkLine(-0.2, true, Seq(0.0f, 1.0f)),
      LinkLine(0.0, true, Seq(1.0f, 0.0f)),
      LinkLine(0.2, true, Seq(0.0f, 1.0f)))

  val defaultDirectionIndicator =
    VectorShape("link direction", true, 0,
      Line(RgbColor(141, 141, 141), true, (150, 150), (90, 180)),
      Line(RgbColor(141, 141, 141), true, (150, 150), (210, 180)))

  def assertParsesLinkLines(shapeDefinition: Seq[String], linkLines: LinkLine*) =
    assertResult(linkLines.toSeq)(ShapeParser.parseLinkShapes(shapeDefinition).head.linkLines)

  test("ShapeParser returns an empty seq when there are no vector shapes") {
    assertResult(ShapeParser.parseVectorShapes(Seq()))(Seq())
  }

  test("ShapeParser returns an empty seq when there are no link shapes") {
    assertResult(Seq[LinkShape]())(ShapeParser.parseLinkShapes(Seq()))
  }

  test("ShapeParser raises an error on invalid data") {
    intercept[IllegalStateException] { ShapeParser.parseVectorShapes(Seq("oogabooga")) }
  }

  test("ShapeParser parses a list of one shape - a dot") {
    assertParsesTo(
      vectorShapeString("dot", "false", "0", elementStrings("circle")),
      VectorShape("dot", false, 0, elements("circle")))
  }

  test("ShapeParser tolerates trailing whitespace") {
    assertParsesTo(
      vectorShapeString("dot", "false", "0", elementStrings("circle")) ++ Seq(""),
      VectorShape("dot", false, 0, elements("circle")))
  }

  test("ShapeParser formats VectorShapes appropriately") {
    assertFormatsTo(
      vectorShapeString("dot", "false", "0", elementStrings("circle")),
      VectorShape("dot", false, 0, elements("circle")))
  }

  test("ShapeParser parsing a VectorShape with no subshapes is an empty shape") {
    assertParsesTo(vectorShapeString("empty", "false", "0"), VectorShape("empty", false, 0))
  }

  test("ShapeParser parses rotatable to false when not true") {
    assertParsesTo(vectorShapeString("nottrue", "foobar", "0"), VectorShape("nottrue", false, 0))
  }

  test("ShapeParser parses rotatable to true when true") {
    assertParsesTo(vectorShapeString("rotatable", "true", "0"), VectorShape("rotatable", true, 0))
  }

  test("ShapeParser raises an exception for shapes with an invalid color") {
    intercept[java.lang.NumberFormatException] {
      ShapeParser.parseVectorShapes(vectorShapeString("invalidnumber", "true", "ajbqwoern"))
    }
  }

  test("ShapeParser parses out a list of empty-line separated shapes") {
    assertParsesTo(
      vectorShapeString("multishapes", "false", "0",
        elementStrings("circle"), elementStrings("circle")),
      VectorShape("multishapes", false, 0, elements("circle"), elements("circle")))
  }

  val elements: Map[String, Element] = Map(
    "circle"       -> Circle(RgbColor(141, 141, 141), true, true, 90, 90, 120),
    "line"         -> Line(RgbColor(141, 141, 141), true, (150, 0), (150, 300)),
    "polygon"      ->
      Polygon(RgbColor(0, 0, 0), true, false, (238, 112), (252, 141), (219, 141), (218, 112)),
    "rectangle"    -> Rectangle(RgbColor(141, 141, 141), true, true, (0, 0), (100, 100)),
    "rectangleBig" ->  Rectangle(RgbColor(141, 141, 141), true, false, (0, 0), (200, 200))
    )

  val elementStrings: Map[String, String] = Map(
    "circle"       -> "Circle -7500403 true true 90 90 120",
    "line"         -> "Line -7500403 true 150 0 150 300",
    "lineInvalid"  -> "Line -7500403 abasoniqeworn 150 0 150 300",
    "polygon"      -> "Polygon -16777216 true false 238 112 252 141 219 141 218 112",
    "rectangle"    -> "Rectangle -7500403 true true 0 0 100 100",
    "rectangleBig" -> "Rectangle -7500403 true false 0 0 200 200"
  )

  Seq("line", "rectangle", "circle", "polygon").foreach { elemType =>
    test(s"ShapeParser parses $elemType elements") {
      assertParsesTo(
        vectorShapeString(elemType, "false", "0", elementStrings(elemType)),
        VectorShape(elemType, false, 0, elements(elemType)))
    }

    test(s"ShapeParser formats $elemType elements") {
      assertFormatsTo(
        vectorShapeString(elemType, "false", "0", elementStrings(elemType)),
        VectorShape(elemType, false, 0, elements(elemType)))
    }
  }

  test("ShapeParser raises an exception on invalid shape values") {
    intercept[IllegalStateException] {
      ShapeParser.parseVectorShapes(
        vectorShapeString("invalidLine", "false", "0", elementStrings("lineInvalid")))
    }
  }

  test("ShapeParser raises an exception on invalid number of shape fields") {
    intercept[IllegalStateException] {
      ShapeParser.parseVectorShapes(
        vectorShapeString("invalidshapecount", "false", "0", "Line -7500403 true 150 0 25"))
    }
  }

  test("ShapeParser raises an exception on invalid shape names") {
    intercept[IllegalStateException] {
      ShapeParser.parseVectorShapes(
        vectorShapeString("invalidshapetype", "false", "0", "Rhombus -7500403 true 150 0 25 25"))
    }
  }

  test("shapeParser parses multiple vector shapes") {
    val twoRects =
      vectorShapeString("box", "true", "0", elementStrings("rectangle")) ++ Seq("") ++
        vectorShapeString("box2", "true", "0", elementStrings("rectangleBig"))
    assertParsesTo(twoRects,
      VectorShape("box", true, 0, elements("rectangle")),
      VectorShape("box2", true, 0, elements("rectangleBig")))
  }

  test("shapeParser formats multiple vector shapes") {
    val twoRects =
      vectorShapeString("box", "true", "0", elementStrings("rectangle")) ++ Seq("") ++
        vectorShapeString("box2", "true", "0", elementStrings("rectangleBig"))
    assertFormatsTo(twoRects,
      VectorShape("box", true, 0, elements("rectangle")),
      VectorShape("box2", true, 0, elements("rectangleBig")))
  }

  test("ShapeParser raises an exception on invalid link shapes") {
    intercept[IllegalStateException] {
      ShapeParser.parseLinkShapes(Seq("foobar", "0.0"))
    }
  }

  test("ShapeParser parses a valid link shape into a LinkShape") {
    assertResult(Seq(LinkShape("default", 0.0, defaultLinkLines, defaultDirectionIndicator)))(
      ShapeParser.parseLinkShapes(linkShapeString("default")))
  }

  test("shapeParser raises an error for invalid link lines") {
    intercept[IllegalStateException] {
      ShapeParser.parseLinkShapes(Seq("default", "invalidvalue", "-0.2 0 foobar 1.0")) }
  }

  test("shapeParser raises an exception on invalid linkLines") {
    intercept[IllegalStateException] {
      ShapeParser.parseLinkShapes(linkShapeString(name = "foobar", linkLines = Seq("foo ooo bar", "-,.,-", "\u1234")))
    }
  }

  test("shapeParser parses multiple link shapes") {
    assertResult(
      Seq(LinkShape("default", 0.0, defaultLinkLines, defaultDirectionIndicator),
        LinkShape("default2", 0.0, defaultLinkLines, defaultDirectionIndicator)))(
        ShapeParser.parseLinkShapes(linkShapeString("default") ++ Seq("") ++ linkShapeString("default2")))
    }

  test("shapeParser parses a link with no name if it's the second link") {
    assertResult(
      Seq(LinkShape("default", 0.0, defaultLinkLines, defaultDirectionIndicator),
        LinkShape("", 0.0, defaultLinkLines, defaultDirectionIndicator)))(
      ShapeParser.parseLinkShapes(linkShapeString() ++ Seq("") ++ linkShapeString(name = ""))
    )
  }

  test("shapeParser parses link lines to have any of the permitted dash values") {
    val dashedLinkLines = linkShapeString(name = "dashedLines",
      linkLines = Seq("-0.2 1 2.0 2.0", "0.0 1 4.0 4.0", "0.2 1 4.0 4.0 2.0 2.0"))
    assertParsesLinkLines(dashedLinkLines,
      LinkLine(-0.2, true, Seq(2.0f, 2.0f)),
      LinkLine(0.0, true, Seq(4.0f, 4.0f)),
      LinkLine(0.2, true, Seq(4.0f, 4.0f, 2.0f, 2.0f)))
  }

  test("shapeParser parses invalid link line values to be solid lines") {
    val invalidLinkLines = linkShapeString(name = "dashedLines",
      linkLines = Seq("-0.2 1 3.0 3.0", "0.0 1 2.0 4.0", "0.2 1 6.0 7.0 13.0 21.0"))
    assertParsesLinkLines(invalidLinkLines,
      LinkLine(-0.2, true, Seq(1.0f, 0.0f)),
      LinkLine(0.0, true, Seq(1.0f, 0.0f)),
      LinkLine(0.2, true, Seq(1.0f, 0.0f)))
  }

  test("shapeParser parses invisible link lines to be dashed invisibly") {
    val invisibleLinkLines = linkShapeString(name = "invisibleLines",
      linkLines = Seq("-0.2 0 1.0 0.0", "0.0 0 2.0 2.0", "0.2 0 4.0 4.0"))
    assertParsesLinkLines(invisibleLinkLines,
      LinkLine(-0.2, false, Seq(0.0f, 1.0f)),
      LinkLine(0.0, false, Seq(0.0f, 1.0f)),
      LinkLine(0.2, false, Seq(0.0f, 1.0f)))
  }

  test("shapeParser formats linkShapes correctly") {
    assertResult(
      (linkShapeString("default") ++ Seq("") ++ linkShapeString("default2")).mkString("\n"))(
        ShapeParser.formatLinkShapes(
          Seq(LinkShape("default", 0.0, defaultLinkLines, defaultDirectionIndicator),
            LinkShape("default2", 0.0, defaultLinkLines, defaultDirectionIndicator))))
  }
}
