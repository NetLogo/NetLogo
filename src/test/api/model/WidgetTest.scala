// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api.model

import org.scalatest.FunSuite
import org.nlogo.headless.HeadlessWorkspace

class WidgetTest extends FunSuite {

  test("Required reader lines") {
    val ws = HeadlessWorkspace.newInstance
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
      def asAnyRef(vals: List[Any]): TestWidget = TestWidget(vals)
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
    val ws = HeadlessWorkspace.newInstance
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
      def asAnyRef(vals: List[Any]): TestWidget = TestWidget(vals)
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
  }
  test("view") {
  }
  test("monitor") {
  }
  test("switch") {
  }
  test("plot") {
  }
  test("chooser") {
  }
  test("output") {
  }
  test("textbox") {
  }
  test("inputbox color") {
  }
  test("inputbox num") {
  }
  test("inputbox str") {
  }
  test("inputbox str reporter") {
  }
  test("inputbox str command") {
  }
}
