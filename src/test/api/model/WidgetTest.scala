// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api
package model

import org.nlogo.api
import org.nlogo.core._
import org.scalatest.FunSuite
import org.nlogo.headless.HeadlessWorkspace

class WidgetTest extends FunSuite {

  test("Required reader lines") {
    case class TestWidget(vals: List[Any])  extends Widget
    object AllLineTest extends BaseWidgetReader {
      type T = TestWidget
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
      def asWidget(vals: List[Any]): TestWidget = TestWidget(vals)
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
    assert((List((), 1, 3, "4a", 5.0, true, true, true, true, ()) ==
            AllLineTest.parse(fullAllLineTest).vals))
    assert((List((), 1, 3, "4a", 6.0, true, true, true, true, ()) !=
            AllLineTest.parse(fullAllLineTest).vals))

  }

  test("Unrequired reader lines") {
    case class TestWidget(vals: List[Any])  extends Widget
    object AllLineTest extends BaseWidgetReader {
      type T = TestWidget
      def definition = List(new SpecifiedLine("B"),
                            IntLine(Some(1)),
                            StringLine(Some("2")),
                            DoubleLine(Some(3.0)),
                            BooleanLine(Some(true)),
                            TNilBooleanLine(Some(true)),
                            InvertedBooleanLine(Some(true)),
                            StringBooleanLine(Some(true)))
      def asList(t: TestWidget): List[Any] = t.vals
      def asWidget(vals: List[Any]): TestWidget = TestWidget(vals)
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
            AllLineTest.parse(fullAllLineTest).vals))
    assert(AllLineTest.validate(partialAllLineTest))
    assert((List((), 5, "6b", 3.0, true, true, true, true) ==
            AllLineTest.parse(partialAllLineTest).vals))
    assert(AllLineTest.validate(minimalAllLineTest))
    assert((List((), 1, "2", 3.0, true, true, true, true) ==
            AllLineTest.parse(minimalAllLineTest).vals))
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
    assert(ButtonReader.validate(button))
    assert(Button("go",202,101,271,134,"go",true) == ButtonReader.parse(button))
    assert(ButtonReader.validate(ButtonReader.format(ButtonReader.parse(button)).split("\n").toList))
    assert(Button("go",202,101,271,134,"go",true) ==
      ButtonReader.parse(ButtonReader.format(ButtonReader.parse(button)).split("\n").toList))
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
    assert(SliderReader.validate(slider))
    assert(Slider("initial-sheep-stride", 20, 65, 201, 98, "initial-sheep-stride", "0", "1", 0.2, "0.1", "NIL", Horizontal) ==
      SliderReader.parse(slider))
    assert(SliderReader.validate(SliderReader.format(SliderReader.parse(slider)).split("\n").toList))
    assert(Slider("initial-sheep-stride", 20, 65, 201, 98, "initial-sheep-stride", "0", "1", 0.2, "0.1", "NIL", Horizontal) ==
      SliderReader.parse(SliderReader.format(SliderReader.parse(slider)).split("\n").toList))
      
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
                  |1
                  |1
                  |1
                  |ticks
                  |30.0""".stripMargin.split("\n").toList

    assert(ViewReader.validate(view))
    assert(View(430, 12, 806, 409, 6.0, 20, true, true, -30, 30, -30, 30, UpdateMode.Continuous, true, "ticks", 30.0) ==
      ViewReader.parse(view))
    assert(ViewReader.validate(ViewReader.format(ViewReader.parse(view)).split("\n").toList))
    assert(View(430, 12, 806, 409, 6.0, 20, true, true, -30, 30, -30, 30, UpdateMode.Continuous, true, "ticks", 30.0) ==
      ViewReader.parse(ViewReader.format(ViewReader.parse(view)).split("\n").toList))
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
    assert(MonitorReader.validate(monitor))
    assert(Monitor("sheep", 74, 214, 152, 259, "count sheep", 3, 11) == MonitorReader.parse(monitor))
    assert(MonitorReader.validate(MonitorReader.format(MonitorReader.parse(monitor)).split("\n").toList))
    assert(Monitor("sheep", 74, 214, 152, 259, "count sheep", 3, 11) ==
      MonitorReader.parse(MonitorReader.format(MonitorReader.parse(monitor)).split("\n").toList))
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
    assert(SwitchReader.validate(switch))
    assert(Switch("stride-length-penalty?", 111, 174, 307, 207, "stride-length-penalty?", true) == SwitchReader.parse(switch))
    assert(SwitchReader.validate(SwitchReader.format(SwitchReader.parse(switch)).split("\n").toList))
    assert(Switch("stride-length-penalty?", 111, 174, 307, 207, "stride-length-penalty?", true) ==
      SwitchReader.parse(SwitchReader.format(SwitchReader.parse(switch)).split("\n").toList))

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
    assert(PlotReader.validate(plot))
    assert(Plot("populations", 33, 265, 369, 408, "time", "pop.", 0.0, 100.0, 0.0, 100.0, true, true, "", "",
      List(Pen("sheep", 1.0, 0, -13345367, true, "", "plot count sheep"), 
           Pen("wolves", 1.0, 0, -2674135, true, "", "plot count wolves"), 
           Pen("grass / 4", 1.0, 0, -10899396, true, "", ";; divide by four to keep it within similar\n;; range as wolf and sheep populations\nplot count patches with [ pcolor = green ] / 4"))) ==
         PlotReader.parse(plot))
    assert(PlotReader.validate(PlotReader.format(PlotReader.parse(plot)).split("\n").toList))
    assert(Plot("populations", 33, 265, 369, 408, "time", "pop.", 0.0, 100.0, 0.0, 100.0, true, true, "", "",
      List(Pen("sheep", 1.0, 0, -13345367, true, "", "plot count sheep"), 
           Pen("wolves", 1.0, 0, -2674135, true, "", "plot count wolves"), 
           Pen("grass / 4", 1.0, 0, -10899396, true, "", ";; divide by four to keep it within similar\n;; range as wolf and sheep populations\nplot count patches with [ pcolor = green ] / 4"))) ==
      PlotReader.parse(PlotReader.format(PlotReader.parse(plot)).split("\n").toList))
  }

  test("chooser") {
    val ps = new api.DummyParserServices()
    val chooser = """|CHOOSER
                     |164
                     |10
                     |315
                     |55
                     |visualize-time-steps
                     |visualize-time-steps
                     |"days" "years"
                     |1""".stripMargin.split("\n").toList
    val cr = new ChooserReader(ps)
    assert(cr.validate(chooser))
    assert(Chooser("visualize-time-steps", 164, 10, 315, 55, "visualize-time-steps", List("days", "years"), 1) ==
      cr.parse(chooser))
    assert(cr.validate(cr.format(cr.parse(chooser)).split("\n").toList))
    assert(Chooser("visualize-time-steps", 164, 10, 315, 55, "visualize-time-steps", List("days", "years"), 1) ==
      cr.parse(cr.format(cr.parse(chooser)).split("\n").toList))
  }

  test("output") {
    val output = """|OUTPUT
                    |290
                    |449
                    |602
                    |543
                    |12""".stripMargin.split("\n").toList
    assert(OutputReader.validate(output))
    assert(Output(290, 449, 602, 543, 12) == OutputReader.parse(output))
    assert(OutputReader.validate(OutputReader.format(OutputReader.parse(output)).split("\n").toList))
    assert(Output(290, 449, 602, 543, 12) == OutputReader.parse(OutputReader.format(OutputReader.parse(output)).split("\n").toList))
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
    assert(TextBoxReader.validate(textBox))
    assert(TextBox("Sheep settings", 28, 11, 168, 30, 11, 0.0, false) == TextBoxReader.parse(textBox))
    assert(TextBoxReader.validate(TextBoxReader.format(TextBoxReader.parse(textBox)).split("\n").toList))
    assert(TextBox("Sheep settings", 28, 11, 168, 30, 11, 0.0, false) ==
      TextBoxReader.parse(TextBoxReader.format(TextBoxReader.parse(textBox)).split("\n").toList))
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
    val ibr = new InputBoxReader()
    assert(ibr.validate(inputBox))
    assert(InputBox(119, 309, 274, 369, "fgcolor", 123, true, Col) == ibr.parse(inputBox))
    assert(ibr.validate(ibr.format(ibr.parse(inputBox)).split("\n").toList))
    assert(InputBox(119, 309, 274, 369, "fgcolor", 123, true, Col) ==
      ibr.parse(ibr.format(ibr.parse(inputBox)).split("\n").toList))
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

    val ibr = new InputBoxReader()
    assert(ibr.validate(inputBox))
    assert(InputBox(31, 301, 112, 361, "step-size", 1.0, true, Num) == ibr.parse(inputBox))
    assert(ibr.validate(ibr.format(ibr.parse(inputBox)).split("\n").toList))
    assert(InputBox(31, 301, 112, 361, "step-size", 1.0, true, Num) ==
      ibr.parse(ibr.format(ibr.parse(inputBox)).split("\n").toList))
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

    val ibr = new InputBoxReader()
    assert(ibr.validate(inputBox))
    assert(InputBox(5, 330, 255, 390, "user-created-code", "AAAAA", true, Str) == ibr.parse(inputBox))
    assert(ibr.validate(ibr.format(ibr.parse(inputBox)).split("\n").toList))
    assert(InputBox(5, 330, 255, 390, "user-created-code", "AAAAA", true, Str) ==
      ibr.parse(ibr.format(ibr.parse(inputBox)).split("\n").toList))
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

    val ibr = new InputBoxReader()
    assert(ibr.validate(inputBox))
    assert(InputBox(245, 134, 470, 214, "my-equation", "0", true, StrReporter) == ibr.parse(inputBox))
    assert(ibr.validate(ibr.format(ibr.parse(inputBox)).split("\n").toList))
    assert(InputBox(245, 134, 470, 214, "my-equation", "0", true, StrReporter) ==
      ibr.parse(ibr.format(ibr.parse(inputBox)).split("\n").toList))
  }
}
