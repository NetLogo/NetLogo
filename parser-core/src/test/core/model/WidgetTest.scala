// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core.model

import org.nlogo.core.{ model, TextBox, Output, ChooseableList, CompilerException,
  LiteralParser, LogoList, Nobody, NumericInput,
  NumberParser, InputBox, Chooser, ChooseableString, Pen, Plot, Switch,
  Monitor, UpdateMode, View, Horizontal, Button, Slider, StringInput,
  Widget, WorldDimensions },
  model._
import org.scalatest.FunSuite
import scala.reflect.ClassTag

object SimpleLiteralParser extends LiteralParser {
  override def readFromString(s: String): AnyRef =
    if (s == "nobody")
      Nobody
    else if (s.startsWith("[") && s.endsWith("]"))
      if (s(1) == '[')
        LogoList(readFromString(s.drop(1).dropRight(1)))
      else {
        val parts = s.drop(1).dropRight(1).split(' ').filterNot(_.trim.length == 0)
        LogoList.fromVector(parts.map(readFromString).toVector)
      }
    else if (s.startsWith("\"") && s.endsWith("\""))
      s.drop(1).dropRight(1)
    else
      readNumberFromString(s)

  override def readNumberFromString(source: String): AnyRef = {
    NumberParser.parse(source).right.getOrElse(
      throw new CompilerException(source, 0, 1, s"invalid number: $source"))
  }
}

class WidgetTest extends FunSuite {

  val literalParser = SimpleLiteralParser

  case class TestWidget(vals: List[Any]) extends Widget {
    def left = 0
    def top = 0
    def right = 5
    def bottom = 5
  }

  test("Required reader lines") {
    object AllLineTest extends BaseWidgetReader {
      type T = TestWidget
      def classTag = ClassTag(classOf[TestWidget])
      def definition = List(new SpecifiedLine("A"),
                            MapLine(List(("a", 1), ("b", 2))),
                            IntLine(),
                            StringLine(),
                            DoubleLine(),
                            BooleanLine(),
                            TNilBooleanLine(),
                            InvertedBooleanLine(),
                            StringBooleanLine(),
                            ReservedLine())
      def asList(t: TestWidget): List[Any] = t.vals
      def asWidget(vals: List[Any], literalParser: LiteralParser): TestWidget = TestWidget(vals)
    }
    val fullAllLineTest = """|A
                             |b
                             |3
                             |4a
                             |5
                             |1
                             |T
                             |0
                             |true
                             |alksdjflkasdjf""".stripMargin.split("\n").toList

    assert(!AllLineTest.validate(fullAllLineTest.updated(0, "C")))
    assert(!AllLineTest.validate(fullAllLineTest.updated(1, "d")))
    assert(!AllLineTest.validate(fullAllLineTest.updated(2, "x")))
    assert(!AllLineTest.validate(fullAllLineTest.updated(4, "mC")))
    assert(!AllLineTest.validate(fullAllLineTest.updated(5, "T")))
    assert(!AllLineTest.validate(fullAllLineTest.updated(6, "0")))
    assert(!AllLineTest.validate(fullAllLineTest.updated(7, "8")))
    assert(AllLineTest.validate(fullAllLineTest))
    assert((List((), 2, 3, "4a", 5.0, true, true, true, true, ()) ==
            AllLineTest.parse(fullAllLineTest, literalParser).vals))
    assert((List((), 2, 3, "4a", 6.0, true, true, true, true, ()) !=
            AllLineTest.parse(fullAllLineTest, literalParser).vals))

  }


  test("Unrequired reader lines") {
    object AllLineTest extends BaseWidgetReader {
      type T = TestWidget
      def classTag = ClassTag(classOf[TestWidget])
      def definition = List(new SpecifiedLine("B"),
                            IntLine(Some(1)),
                            StringLine(Some("2")),
                            DoubleLine(Some(3.0)),
                            BooleanLine(Some(true)),
                            TNilBooleanLine(Some(true)),
                            InvertedBooleanLine(Some(true)),
                            StringBooleanLine(Some(true)))
      def asList(t: TestWidget): List[Any] = t.vals
      def asWidget(vals: List[Any], literalParser: LiteralParser): TestWidget = TestWidget(vals)
    }
    val fullAllLineTest = """|B
                             |5
                             |6b
                             |7.0
                             |0
                             |NIL
                             |1
                             |false""".stripMargin.split("\n").toList
    val partialAllLineTest = """|B
                                |5
                                |6b""".stripMargin.split("\n").toList
    val minimalAllLineTest = """|B""".stripMargin.split("\n").toList

    assert(AllLineTest.validate(fullAllLineTest))
    assert((List((), 5, "6b", 7.0, false, false, false, false) ==
            AllLineTest.parse(fullAllLineTest, literalParser).vals))
    assert(AllLineTest.validate(partialAllLineTest))
    assert((List((), 5, "6b", 3.0, true, true, true, true) ==
            AllLineTest.parse(partialAllLineTest, literalParser).vals))
    assert(AllLineTest.validate(minimalAllLineTest))
    assert((List((), 1, "2", 3.0, true, true, true, true) ==
            AllLineTest.parse(minimalAllLineTest, literalParser).vals))
  }

  test("button") {
    val button = """|BUTTON
                    |202
                    |101
                    |271
                    |134
                    |go
                    |go
                    |T
                    |1
                    |T
                    |OBSERVER
                    |NIL
                    |NIL
                    |NIL
                    |NIL
                    |1""".stripMargin.split("\n").toList
    val buttonWidget =
      Button(Some("go"),202,101,271,134,Some("go"),true)
    runSerializationTests(button, buttonWidget, ButtonReader)
  }

  test("button nil display") {
    val button = """|BUTTON
                    |202
                    |101
                    |271
                    |134
                    |NIL
                    |go
                    |T
                    |1
                    |T
                    |OBSERVER
                    |NIL
                    |NIL
                    |NIL
                    |NIL
                    |1""".stripMargin.split("\n").toList
    val buttonWidget =
      Button(Some("go"),202,101,271,134,None,true)
    runSerializationTests(button, buttonWidget, ButtonReader)
  }

  test("button escaped source") {
    val button = """|BUTTON
                    |202
                    |101
                    |271
                    |134
                    |NIL
                    |\"bar\"
                    |T
                    |1
                    |T
                    |OBSERVER
                    |NIL
                    |NIL
                    |NIL
                    |NIL
                    |1""".stripMargin.split("\n").toList
    val buttonWidget = Button(Some("\"bar\""),202,101,271,134,None,true)
    runSerializationTests(button, buttonWidget, ButtonReader)
  }

  test("button with action key") {
    val button = """|BUTTON
                    |0
                    |0
                    |5
                    |5
                    |NIL
                    |bar
                    |T
                    |1
                    |T
                    |OBSERVER
                    |NIL
                    |I
                    |NIL
                    |NIL
                    |1""".stripMargin.split("\n").toList
    val buttonWidget = Button(Some("bar"),0,0,5,5,None,true, actionKey = Some('I'))
    runSerializationTests(button, buttonWidget, ButtonReader)
  }
  test("button disabled until ticks start") {
    val button = """|BUTTON
                    |202
                    |101
                    |271
                    |134
                    |NIL
                    |\"bar\"
                    |T
                    |1
                    |T
                    |OBSERVER
                    |NIL
                    |NIL
                    |NIL
                    |NIL
                    |0""".stripMargin.split("\n").toList
    val buttonWidget = Button(Some("\"bar\""),202,101,271,134,None,true,disableUntilTicksStart = true)
    runSerializationTests(button, buttonWidget, ButtonReader, {(button: Button) => assert(button.display == None)})
  }

  test("button with source conversion") {
    val button = """|BUTTON
                    |202
                    |101
                    |271
                    |134
                    |NIL
                    |\"bar\"
                    |T
                    |1
                    |T
                    |OBSERVER
                    |NIL
                    |NIL
                    |NIL
                    |NIL
                    |1""".stripMargin.split("\n").toList
    val buttonWidget = Button(Some("\"bar\"\"bar\""),202,101,271,134,None,true)
    val deserializedWidget = WidgetReader.read(button, literalParser, conversion = (x => x + x))
    assertResult(buttonWidget)(deserializedWidget)
  }

  test("slider") {
     val slider = """|SLIDER
                     |20
                     |65
                     |201
                     |98
                     |initial-sheep-stride
                     |initial-sheep-stride
                     |0
                     |1
                     |0.2
                     |0.1
                     |1
                     |NIL
                     |HORIZONTAL""".stripMargin.split("\n").toList
    val sliderWidget = Slider(Some("initial-sheep-stride"), 20, 65, 201, 98, Some("initial-sheep-stride"), "0", "1", 0.2, "0.1", None, Horizontal)
    runSerializationTests(slider, sliderWidget, SliderReader)
  }

  test("view") {
    val view = """|GRAPHICS-WINDOW
                  |430
                  |12
                  |806
                  |409
                  |30
                  |30
                  |6.0
                  |1
                  |20
                  |1
                  |1
                  |1
                  |0
                  |1
                  |1
                  |1
                  |-30
                  |30
                  |-30
                  |30
                  |0
                  |0
                  |1
                  |ticks
                  |30.0""".stripMargin.split("\n").toList


    val viewWidget = View(430, 12, 806, 409, new WorldDimensions(-30, 30, -30, 30, patchSize = 6.0, true, true), fontSize = 20, UpdateMode.Continuous, true, Some("ticks"), 30.0)
    runSerializationTests(view, viewWidget, ViewReader)
  }

  test("tick-based view") {
    val view = View(updateMode = UpdateMode.TickBased)
    assertResult(view)(ViewReader.parse(ViewReader.format(view).lines.toList, literalParser))
  }

  test("monitor") {
    val monitor = """|MONITOR
                     |74
                     |214
                     |152
                     |259
                     |sheep
                     |count sheep
                     |3
                     |1
                     |11""".stripMargin.split("\n").toList
    val monitorWidget = Monitor(Some("count sheep"), 74, 214, 152, 259, Some("sheep"), 3, 11)
    runSerializationTests(monitor, monitorWidget, MonitorReader)
  }

  test("monitor nil display") {
    val monitor = """|MONITOR
                     |74
                     |214
                     |152
                     |259
                     |NIL
                     |count sheep
                     |3
                     |1
                     |11""".stripMargin.split("\n").toList
    val monitorWidget = Monitor(Some("count sheep"), 74, 214, 152, 259, None, 3, 11)
    runSerializationTests(monitor, monitorWidget, MonitorReader, { (m: Monitor) => assert(None == m.display) })
  }

  test("monitor nil display escaped source") {
    val monitor = """|MONITOR
                     |74
                     |214
                     |152
                     |259
                     |NIL
                     |\"foo\"
                     |3
                     |1
                     |11""".stripMargin.split("\n").toList
    val monitorWidget = Monitor(Some("\"foo\""), 74, 214, 152, 259, None, 3, 11)
    runSerializationTests(monitor, monitorWidget, MonitorReader)
  }

  test("switch") {
    val switch = """|SWITCH
                    |111
                    |174
                    |307
                    |207
                    |stride-length-penalty?
                    |stride-length-penalty?
                    |0
                    |1
                    |-1000""".stripMargin.split("\n").toList
    val switchWidget = Switch(Some("stride-length-penalty?"), 111, 174, 307, 207, Some("stride-length-penalty?"), true)
    runSerializationTests(switch, switchWidget, SwitchReader)
  }

  test("plot") {
    val plot = """|PLOT
                  |33
                  |265
                  |369
                  |408
                  |populations
                  |time
                  |pop.
                  |0.0
                  |100.0
                  |0.0
                  |100.0
                  |true
                  |true
                  |"" ""
                  |PENS
                  |"sheep" 1.0 0 -13345367 true "" "plot count sheep"
                  |"wolves" 1.0 0 -2674135 true "" "plot count wolves"
                  |"grass / 4" 1.0 0 -10899396 true "" ";; divide by four to keep it within similar\n;; range as wolf and sheep populations\nplot count patches with [ pcolor = green ] / 4" """.stripMargin.split("\n").toList
    val plotWidget =
      Plot(Some("populations"), 33, 265, 369, 408, Some("time"), Some("pop."), 0.0, 100.0, 0.0, 100.0, true, true, "", "",
        List(Pen("sheep", 1.0, 0, -13345367, true, "", "plot count sheep"),
          Pen("wolves", 1.0, 0, -2674135, true, "", "plot count wolves"),
          Pen("grass / 4", 1.0, 0, -10899396, true, "", ";; divide by four to keep it within similar\n;; range as wolf and sheep populations\nplot count patches with [ pcolor = green ] / 4")))
    runSerializationTests(plot, plotWidget, PlotReader)
  }

  test("chooser") {
    val chooser = chooserWithChoices(""""days" "years"""")
    val chooserWidget =
      Chooser(Some("visualize-time-steps"), 164, 10, 315, 55, Some("visualize-time-steps"), List(ChooseableString("days"), ChooseableString("years")), 1)
    runSerializationTests(chooser, chooserWidget, ChooserReader)
  }

  test("chooser with nobody converts nobody to string") {
    val chooser = chooserWithChoices(""""days" "years" nobody""")
    assert(ChooserReader.validate(chooser))
    ChooserReader.parse(chooser, literalParser) == Chooser(Some("visualize-time-steps"), 164, 10, 315, 55, Some("visualize-time-steps"), List(ChooseableString("days"), ChooseableString("years"), ChooseableString("nobody")), 1)
  }

  test("chooser with nested nobody converts nobody to string") {
    val chooser = chooserWithChoices("""["days" "years" [nobody]]""")
    ChooserReader.parse(chooser, literalParser) == Chooser(Some("visualize-time-steps"), 164, 10, 315, 55, Some("visualize-time-steps"),
      List(ChooseableList(LogoList(
        ChooseableString("days"),
        ChooseableString("years"),
        ChooseableList(LogoList("nobody"))))),
    1)
  }

  private def chooserWithChoices(choices: String): List[String] = {
    s"""|CHOOSER
        |164
        |10
        |315
        |55
        |visualize-time-steps
        |visualize-time-steps
        |$choices
        |1""".stripMargin.split("\n").toList
  }

  test("output") {
    val output = """|OUTPUT
                    |290
                    |449
                    |602
                    |543
                    |12""".stripMargin.split("\n").toList
    val outputWidget = Output(290, 449, 602, 543, 12)
    runSerializationTests(output, outputWidget, OutputReader)
  }

  test("textbox") {
    val textBox = """|TEXTBOX
                     |28
                     |11
                     |168
                     |30
                     |Sheep settings
                     |11
                     |0.0
                     |0""".stripMargin.split("\n").toList
    val textBoxWidget = TextBox(Some("Sheep settings"), 28, 11, 168, 30, 11, 0.0, false)
    runSerializationTests(textBox, textBoxWidget, TextBoxReader)
  }

  test("textboxWithEscapes") {
    val textBox = """|TEXTBOX
                     |18
                     |95
                     |168
                     |151
                     |Note, with	tabs and\n\nnewlines and\nescaped newlines \"\\n\"
                     |11
                     |0.0
                     |1""".stripMargin.split("\n").toList
    val escapedText = "Note, with\ttabs and\n\nnewlines and\nescaped newlines \"\\n\""
    val textBoxWidget = TextBox(Some(escapedText), 18, 95, 168, 151, 11, 0.0, true)
    runSerializationTests(textBox, textBoxWidget, TextBoxReader)
  }

  test("inputbox color") {
    val inputBox = """|INPUTBOX
                      |119
                      |309
                      |274
                      |369
                      |fgcolor
                      |123
                      |1
                      |0
                      |Color""".stripMargin.split("\n").toList
    val inputBoxWidget = InputBox(Some("fgcolor"), 119, 309, 274, 369, NumericInput(123, NumericInput.ColorLabel))
    runSerializationTests(inputBox, inputBoxWidget, InputBoxReader)
  }

  test("inputbox num") {
    val inputBox = """|INPUTBOX
                      |31
                      |301
                      |112
                      |361
                      |step-size
                      |1
                      |1
                      |0
                      |Number""".stripMargin.split("\n").toList

    val inputBoxWidget = InputBox(Some("step-size"), 31, 301, 112, 361, NumericInput(1.0, NumericInput.NumberLabel))
    runSerializationTests(inputBox, inputBoxWidget, InputBoxReader)
  }

  test("inputbox str") {
    val inputBox = """|INPUTBOX
                      |5
                      |330
                      |255
                      |390
                      |user-created-code
                      |AAAAA
                      |1
                      |0
                      |String""".stripMargin.split("\n").toList
    val inputBoxWidget = InputBox(Some("user-created-code"), 5, 330, 255, 390, StringInput("AAAAA", StringInput.StringLabel, false))
    runSerializationTests(inputBox, inputBoxWidget, InputBoxReader)
  }

  test("inputbox multiline string") {
    val inputBox = """|INPUTBOX
                      |5
                      |330
                      |255
                      |390
                      |user-created-code
                      |abc\n123\n@#$
                      |1
                      |1
                      |String""".stripMargin.split("\n").toList
    val inputBoxWidget = InputBox(Some("user-created-code"), 5, 330, 255, 390, StringInput("abc\n123\n@#$", StringInput.StringLabel, true))
    runSerializationTests(inputBox, inputBoxWidget, InputBoxReader)
  }

  test("inputbox str reporter") {
    val inputBox = """|INPUTBOX
                      |245
                      |134
                      |470
                      |214
                      |my-equation
                      |0
                      |1
                      |0
                      |String (reporter)""".stripMargin.split("\n").toList
    val inputBoxWidget = InputBox(Some("my-equation"), 245, 134, 470, 214, StringInput("0", StringInput.ReporterLabel, false))
    runSerializationTests(inputBox, inputBoxWidget, InputBoxReader)
  }

  def runSerializationTests[W <: Widget](serializedLines: List[String], widget: W, reader: WidgetReader, extraAssertions: W => Unit = {w: W => })(implicit ev: reader.T =:= W) = {
    assert(reader.validate(serializedLines))
    val deserializedWidget =
      reader.parse(serializedLines, literalParser)
    assertResult(widget)(deserializedWidget)
    extraAssertions(deserializedWidget)
    val reserializedLines =
      reader.format(deserializedWidget).split("\n").toList
    assert(reader.validate(reserializedLines))
    assert(widget == reader.parse(reserializedLines, literalParser))
  }
}
