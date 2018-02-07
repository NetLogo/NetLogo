// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core.model

import org.nlogo.core.{ AgentKind, Button, Chooser,
  ChooseableBoolean, ChooseableDouble, ChooseableList, ChooseableString,
  Horizontal, InputBox, LogoList, Monitor, NumericInput, Output, Pen, Plot, Slider,
  StringInput, Switch, TextBox, UpdateMode, View, Widget, WorldDimensions, WorldDimensions3D }

import org.nlogo.xmllib.{ DummyXml, Element, InvalidAttribute, MissingElement, MissingKeys, MissingValues, ParseError, UnknownElementType },
  DummyXml._

import
  org.scalatest.{ FunSuite, Matchers },
    Matchers._

object WidgetXmlTest {

  val color = Attr("color", "#000000")

  val dimensions =
    Seq(Attr("left", "150"),
      Attr("top", "200"),
      Attr("right", "250"),
      Attr("bottom", "300"))

  val fontSize = Attr("fontSize", "12")

  private val textboxXml = Elem("textbox",
    dimensions ++ Seq(fontSize, color, Attr("transparent", "false")),
    Seq(namedText("display", "Wolf Settings")))

  private val textboxWidget = TextBox(Some("Wolf Settings"), 150, 200, 250, 300, 12, 0.0, false)


  def viewDimensionsXml(name: String, additionalAttrs: Seq[Attr]): Elem = {
    Elem(name,
      additionalAttrs ++
      Seq(
        Attr("minPxcor", "-25"),
        Attr("maxPxcor", "25"),
        Attr("wrapInX", "true"),
        Attr("minPycor", "-25"),
        Attr("maxPycor", "25"),
        Attr("wrapInY", "true"),
        Attr("patchSize", "13.0")),
      Seq())
  }

  val viewDimensions2dXml = viewDimensionsXml("dimensions", Seq())

  val viewDimensions3dXml = viewDimensionsXml("dimensions3d",
    Seq(
      Attr("minPzcor", "-25"),
      Attr("maxPzcor", "25"),
      Attr("wrapInZ", "true")))

  def viewXml(viewDimXml: Elem) =
    Elem("view",
      dimensions ++ Seq(
        Attr("updateMode", "continuous"),
        Attr("fontSize", "12"),
        Attr("frameRate", "30.0"),
        Attr("showTickCounter", "true")),
      Seq(viewDimXml, namedText("tickCounterLabel", "ticks")))

  val viewXml2d = viewXml(viewDimensions2dXml)
  val viewXml3d = viewXml(viewDimensions3dXml)

  val dimensions2d = WorldDimensions(-25, 25, -25, 25, 13.0, true, true)
  val dimensions3d = WorldDimensions3D(-25, 25, -25, 25, -25, 25, 13.0, true, true, true)

  val viewWidget2d = View(150, 200, 250, 300, dimensions2d, 12, UpdateMode.Continuous, true, Some("ticks"), 30.0)
  val viewWidget3d = viewWidget2d.copy(dimensions = dimensions3d)

  val widgetXmlPairs = Map[String, (Element, Widget)](
    "textbox" -> (textboxXml -> textboxWidget),
    "view2d" -> (viewXml2d -> viewWidget2d)
  )

  val penXml =
    Elem("pen",
      Seq(Attr("interval", "0.5"), Attr("mode", "line"),
        Attr("color", "10"), Attr("inLegend", "false")),
      Seq(namedText("setup", "setup-pen"),
        namedText("update", "update-pen"),
        namedText("display", "pen-name")))
}

class WidgetXmlTest extends FunSuite with XmlEquality {
  import WidgetXmlTest._

  def readToWidget(xml: Element): Widget =
    WidgetXml.read(xml).toOption.get

  def readToError(xml: Element): ParseError =
    WidgetXml.read(xml).swap.toOption.get

  def writeToXml(w: Widget): Element =
    WidgetXml.write(w, Factory)

  def format(e: Element): String = formatXml(e)

  def xmlWriteHint(expected: Element, actual: Element): String =
    s"expected:\n${format(expected)}\ngot:\n${format(actual)}"

  test("reads TextBox widgets from xml") {
    assertResult(textboxWidget)(readToWidget(textboxXml))
  }

  test("writes TextBox widgets to xml") {
    textboxXml should beXmlEqualTo (writeToXml(textboxWidget))
  }

  test("color reader correctly identifies colors") {
    val xml = Elem("textbox",
      dimensions ++ Seq(fontSize,
        Attr("color", "#FFFFFF"),
        Attr("transparent", "false")),
      Seq(namedText("display", "Wolf Settings")))
    assertResult(TextBox(Some("Wolf Settings"), 150, 200, 250, 300, 12, 9.9, false))(readToWidget(xml))
  }

  test("transparent reader accepts only true/false") {
    val xml = Elem("textbox",
      dimensions ++ Seq(fontSize, color, Attr("transparent", "abc")),
      Seq(namedText("display", "Wolf Settings")))
    assertResult(InvalidAttribute(Seq("textbox"), "transparent", "abc"))(readToError(xml))
  }

  {
    val attrs = dimensions ++ Seq(fontSize, color, Attr("transparent", "false"))
    val emptyCases =
      Seq(
        ("textbox node contains no children",
          Elem("textbox", attrs, Seq())),
        ("textbox contains children, but no display element",
          Elem("textbox", attrs, Seq(Elem("whatever", Seq(), Seq(Txt("Turtles")))))),
        ("textbox contains display, but display has no text",
          Elem("textbox", attrs, Seq(Elem("display", Seq(), Seq())))))

    emptyCases.foreach {
      case (name, xml) =>
        test(s"$name leads to display: None") {
          val emptyTextTextBox = TextBox(None, 150, 200, 250, 300, 12, 0.0, false)
          assertResult(emptyTextTextBox)(readToWidget(xml))
        }
    }
  }

  test("reads switch widgets from xml") {
    val xml = Elem("switch",
      dimensions :+ Attr("isOn", "false"),
      Seq(namedText("variable", "foo")))
    assertResult(Switch(Some("foo"), 150, 200, 250, 300, Some("foo"), false))(readToWidget(xml))
  }

  test("writes switch widgets to xml") {
    val xml = Elem("switch",
      dimensions :+ Attr("isOn", "false"),
      Seq(namedText("variable", "foo")))
    writeToXml(Switch(Some("foo"), 150, 200, 250, 300, Some("foo"), false)) should beXmlEqualTo (xml)
  }

  test("reads switch widgets with empty variable name from xml") {
    val xml = Elem("switch",
      dimensions :+ Attr("isOn", "false"),
      Seq(namedText("variable", "")))
    assertResult(Switch(None, 150, 200, 250, 300, None, false))(readToWidget(xml))
  }

  test("writes switch widgets with empty variable name to xml") {
    val xml = Elem("switch",
      dimensions :+ Attr("isOn", "false"),
      Seq())
    writeToXml(Switch(None, 150, 200, 250, 300, None, false)) should beXmlEqualTo (xml)
  }

  test("reads monitor widgets from xml") {
    val xml = Elem("monitor",
      dimensions ++ Seq(fontSize, Attr("precision", "3")),
      Seq(namedText("source", "5 + 10"),
        namedText("display", "this is the monitor")))
    assertResult(
      Monitor(Some("5 + 10"),
        150, 200, 250, 300,
        Some("this is the monitor"), 3, 12))(readToWidget(xml))
  }

  test("writes monitor widgets to xml") {
    val xml = Elem("monitor",
      dimensions ++ Seq(fontSize, Attr("precision", "3")),
      Seq(namedText("source", "5 + 10"),
        namedText("display", "this is the monitor")))
    writeToXml(
      Monitor(Some("5 + 10"),
        150, 200, 250, 300,
        Some("this is the monitor"), 3, 12)) should beXmlEqualTo (xml)
  }

  test("reads button widgets from xml") {
    val xml = Elem("button",
      dimensions ++ Seq(
        Attr("forever", "false"),
        Attr("agentKind", "observer"),
        Attr("actionKey", "c"),
        Attr("ticksEnabled", "false")),
      Seq(namedText("source", "go 100"),
        namedText("display", "go")))
    assertResult(
      Button(
        Some("go 100"), 150, 200, 250, 300,
        Some("go"), false, AgentKind.Observer,
        Some('c'), false))(readToWidget(xml))
  }

  test("writes button widgets to xml") {
    val xml = Elem("button",
      dimensions ++ Seq(
        Attr("forever", "false"),
        Attr("ticksEnabled", "false")),
      // doesn't write out agentKind because Observer is default
      Seq(namedText("source", "go 100"),
        namedText("display", "go")))
    writeToXml(
      Button(
        Some("go 100"), 150, 200, 250, 300,
        Some("go"), false, AgentKind.Observer,
        None, false)) should beXmlEqualTo (xml)
  }

  test("writes button widgets with actionKeys to xml") {
    val xml = Elem("button",
      dimensions ++ Seq(
        Attr("forever", "false"),
        Attr("ticksEnabled", "false"),
        // doesn't write out agentKind because Observer is default
        Attr("actionKey", "c")),
      Seq(namedText("source", "go 100"),
        namedText("display", "go")))
      writeToXml(Button(
        Some("go 100"), 150, 200, 250, 300,
        Some("go"), false, AgentKind.Observer,
        Some('c'), false)) should beXmlEqualTo (xml)
  }

  test("reads button widgets without an actionKey from xml") {
    val xml = Elem("button",
      dimensions ++ Seq(
        Attr("forever", "false"),
        Attr("agentKind", "turtle"),
        Attr("ticksEnabled", "true")),
      Seq())
    assertResult(Button(None, 150, 200, 250, 300, None, false, AgentKind.Turtle, None, true))(
      readToWidget(xml))
  }

  test("reads slider widgets") {
    val xml = Elem("slider",
      dimensions ++ Seq(
        Attr("direction", "horizontal"),
        Attr("default", "5")),
      Seq(
        namedText("variable", "foo"),
        namedText("minimum", "0"),
        namedText("maximum", "100"),
        namedText("step", "maximum - minimum / 10"),
        namedText("units", "Foozles")))
    assertResult(Slider(Some("foo"), 150, 200, 250, 300, Some("foo"), "0", "100", 5, "maximum - minimum / 10", Some("Foozles"), Horizontal))(
      readToWidget(xml))
  }

  test("writes slider widgets to xml") {
    val xml = Elem("slider",
      dimensions ++ Seq(
        Attr("default", "5.0"),
        Attr("direction", "horizontal")),
      Seq(
        namedText("maximum", "100"),
        namedText("minimum", "0"),
        namedText("step", "maximum - minimum / 10"),
        namedText("units", "Foozles"),
        namedText("variable", "foo")))
      writeToXml(
        Slider(Some("foo"), 150, 200, 250, 300, Some("foo"),
          "0", "100", 5, "maximum - minimum / 10", Some("Foozles"), Horizontal)) should beXmlEqualTo (xml)
  }

  test("reads view widgets") {
    assertResult(viewWidget2d)(readToWidget(viewXml2d))
  }

  test("writes view widget to xml") {
    writeToXml(viewWidget2d) should beXmlEqualTo (viewXml2d)
  }

  test("reads view widgets with 3D dimensions") {
    assertResult(viewWidget3d)(readToWidget(viewXml3d))
  }

  test("writes view widgets with 3D dimensions") {
    writeToXml(viewWidget3d) should beXmlEqualTo (viewXml3d)
  }

  test("reads chooser widgets") {
    val xml = Elem("chooser",
      dimensions :+ Attr("currentChoice", "0"),
      Seq(
        namedText("variable", "foo"),
        Elem("choices", Seq(), Seq(
        namedText("numberChoice", "0.0"),
        namedText("stringChoice", "abc"),
        namedText("booleanChoice", "true"),
        Elem("listChoice", Seq(), Seq(namedText("boolean", "true"), namedText("string", "def")))))))

    assertResult(Chooser(Some("foo"), 150, 200, 250, 300, Some("foo"),
      List(
        ChooseableDouble(Double.box(0.0)),
        ChooseableString("abc"),
        ChooseableBoolean(Boolean.box(true)),
        ChooseableList(LogoList(Boolean.box(true), "def"))),
      0))(readToWidget(xml))
  }

  test("writes chooser widgets") {
    val xml = Elem("chooser",
      dimensions :+ Attr("currentChoice", "0"),
      Seq(
        namedText("variable", "foo"),
        Elem("choices", Seq(), Seq(
        namedText("numberChoice", "0.0"),
        namedText("stringChoice", "abc"),
        namedText("booleanChoice", "true"),
        Elem("listChoice", Seq(),
          Seq(namedText("boolean", "true"), namedText("string", "def")))))))

    val chooser = Chooser(Some("foo"), 150, 200, 250, 300, Some("foo"),
      List(
        ChooseableDouble(Double.box(0.0)),
        ChooseableString("abc"),
        ChooseableBoolean(Boolean.box(true)),
        ChooseableList(LogoList(Boolean.box(true), "def"))),
      0)

    val actual = writeToXml(chooser)
    actual should beXmlEqualTo (xml)
  }

  test("chooser widgets with invalid choices are invalid") {
    val xml = Elem("chooser",
      dimensions :+ Attr("currentChoice", "0"),
      Seq(
        namedText("variable", "foo"),
        Elem("choices", Seq(), Seq(namedText("booleanChoice", "abc")))))
    assertResult(MissingElement(Seq("chooser", "choices"), "booleanChoice or listChoice or numberChoice or stringChoice"))(readToError(xml))
  }

  test("reads string inputbox widgets") {
    val xml = Elem("stringInput",
      dimensions,
      Seq(
        namedText("variable", "foo"),
        namedText("stringData", "abc").copy(attributes = Seq(Attr("kind", "string"), Attr("multiline", "true")))))

    assertResult(
      InputBox(Some("foo"), 150, 200, 250, 300,
        StringInput("abc", StringInput.StringLabel, true)))(
        readToWidget(xml))
  }

  test("writes string inputbox widgets") {
    val xml = Elem("stringInput",
      dimensions,
      Seq(
        namedText("variable", "foo"),
        namedText("stringData", "abc")
          .copy(attributes = Seq(Attr("multiline", "true"), Attr("kind", "string")))))
    val input = InputBox(Some("foo"), 150, 200, 250, 300,
      StringInput("abc", StringInput.StringLabel, true))

    val actual = writeToXml(input)
    assertResult(xml, s"expected:\n${format(xml)}\ngot:\n${format(actual)}")(actual)
  }

  test("reads numeric inputbox widgets") {
    val xml = Elem("numericInput",
      dimensions,
      Seq(
        namedText("variable", "foo"),
        namedText("numericData", "123.4").copy(attributes = Seq(Attr("kind", "number")))))

    assertResult(
      InputBox(Some("foo"), 150, 200, 250, 300,
        NumericInput(123.4, NumericInput.NumberLabel)))(
        readToWidget(xml))
  }

  test("writes numeric inputbox widgets") {
    val xml = Elem("numericInput",
      dimensions,
      Seq(
        namedText("variable", "foo"),
        namedText("numericData", "123.4").copy(attributes = Seq(Attr("kind", "number")))))
    val inputBox = InputBox(Some("foo"), 150, 200, 250, 300, NumericInput(123.4, NumericInput.NumberLabel))

    val actual = writeToXml(inputBox)

    assertResult(xml, xmlWriteHint(xml, actual))(actual)
  }

  test("reads plot widgets") {
    val xml = Elem("plot",
      dimensions ++ Seq(Attr("autoPlotOn", "true"), Attr("legendOn", "true"),
        Attr("xmin", "5"), Attr("xmax", "10"), Attr("ymin", "15"), Attr("ymax", "20")),
      Seq(
        namedText("setup", "setup-plot"),
        namedText("update", "update-plot"),
        namedText("display", "plot-name"),
        namedText("yAxis", "turtles"),
        namedText("xAxis", "time"),
        Elem("pens", Seq(),
          Seq(penXml))))

    // println(readToError(xml))
    val plotResult = readToWidget(xml)
    val pen = Pen("pen-name", 0.5, 0, 10, false, "setup-pen", "update-pen")
    assertResult(
      Plot(Some("plot-name"), 150, 200, 250, 300, Some("time"), Some("turtles"), 5, 10, 15, 20,
        true, true, "setup-plot", "update-plot", List(pen)))(plotResult)
  }

  test("writes plot widgets") {
    val xml = Elem("plot",
      Seq(Attr("autoPlotOn", "true"), Attr("legendOn", "true"),
        Attr("xmin", "5.0"), Attr("xmax", "10.0"), Attr("ymin", "15.0"), Attr("ymax", "20.0")) ++
        dimensions,
      Seq(
        namedText("display", "plot-name"),
        namedText("xAxis", "time"),
        namedText("yAxis", "turtles"),
        namedText("setup", "setup-plot"),
        namedText("update", "update-plot"),
        Elem("pens", Seq(), Seq(penXml))))

    val pen = Pen("pen-name", 0.5, 0, 10, false, "setup-pen", "update-pen")
    val plot = Plot(
      Some("plot-name"), 150, 200, 250, 300, Some("time"), Some("turtles"), 5, 10, 15, 20,
      true, true, "setup-plot", "update-plot", List(pen))

    val actual = writeToXml(plot)

    assertResult(xml, xmlWriteHint(xml, actual))(actual)
  }

  test("reads output widgets from xml") {
    val xml = Elem("output", dimensions :+ fontSize, Seq())
    assertResult(Output(150, 200, 250, 300, 12))(readToWidget(xml))
  }

  test("writes output widget to xml") {
    val base = Output(150, 200, 250, 300, 12)
    val xml = Elem("output", dimensions :+ fontSize, Seq())
    assertResult(xml)(writeToXml(base))
  }

  test("returns an invalid widget parse when a widget is missing required child element") {
    val xml = Elem("plot",
      dimensions ++ Seq(Attr("autoPlotOn", "true"), Attr("legendOn", "true"),
        Attr("xmin", "5"), Attr("xmax", "10"), Attr("ymin", "15"), Attr("ymax", "20")),
      Seq())
    assertResult(
      MissingValues(Seq("plot"), Seq("pens", "setup", "update").map(MissingValues.MissingElem.apply _)))(
        readToError(xml))
  }

  test("returns an invalid widget parse when a widget is missing a required field") {
    val xml = Elem("output",
      dimensions.tail :+ fontSize,
      Seq())
    assert(WidgetXml.read(xml).isInvalid)
    assertResult(MissingKeys(Seq("output"), Seq("left")))(readToError(xml))
  }

  test("returns an invalid widget parse when a widget field has the wrong type") {
    val xml = Elem("output",
      dimensions.tail ++ Seq(Attr("left", "abc"), fontSize),
      Seq())
    assert(WidgetXml.read(xml).isInvalid)
    assertResult(InvalidAttribute(Seq("output"), "left", "abc"))(readToError(xml))
  }

  test("returns an invalid widget parse when a widget is missing multiple required fields") {
    val xml = Elem("output",
      dimensions.tail.tail,
      Seq())
    assert(WidgetXml.read(xml).isInvalid)
    assertResult(MissingKeys(Seq("output"), Seq("fontSize", "left", "top")))(readToError(xml))
  }

  test("returns an invalid widget parse when the widget type is unknown") {
    val xml = Elem("thingamajig", Seq(), Seq())
    assert(WidgetXml.read(xml).isInvalid)
    assertResult(UnknownElementType(Seq("thingamajig")))(readToError(xml))
  }
}
