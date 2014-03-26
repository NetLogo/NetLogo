// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api.model

import org.scalatest.FunSuite
import org.nlogo.util.Utils.getResourceAsString

class ModelReaderTests extends FunSuite {

  /// parseModel

  test("parseModel: empty model has correct version string") {
    val emptyModel = getResourceAsString("/system/empty.nlogo")
    val model = ModelReader.parseModel(emptyModel)
    assert("NetLogo (no version)" === model.version)
  }
  test("parseModel: trailing section with no separator isn't ignored") {
    val model = ModelReader.parseModel("foo\n" + ModelReader.SEPARATOR + "\n" + ModelReader.SEPARATOR + "\nbar\n")
    assert(model.code === List("foo"))
    assert(model.info === List("bar"))
  }
  test("parseModel: missing sections result in empty arrays") {
    val model = ModelReader.parseModel("")
    assert(model.widgets === Nil)
  }

  /// parseWidgets

  test("parseWidgets: one widget, with trailing blank line") {
//    val widgets = ModelReader.parseWidgets(Seq("FOO",""))
//    assert(widgets.size === 1)
//    assert(widgets(0).size === 1)
//    assert(widgets(0)(0) === "FOO")
  }
  test("parseWidgets: one widget, multiple trailing blank lines") {
//    val widgets = ModelReader.parseWidgets(Seq("FOO","","","","",""))
//    assert(widgets.size === 1)
//    assert(widgets(0).size === 1)
//    assert(widgets(0)(0) === "FOO")
  }
  test("parseWidgets: one widget, no blank line after") {
//    val widgets = ModelReader.parseWidgets(Seq("FOO"))
//    assert(widgets.size === 1)
//    assert(widgets(0).size === 1)
//    assert(widgets(0)(0) === "FOO")
  }
  test("parseWidgets: one widget, leading blank lines") {
//    val widgets = ModelReader.parseWidgets(Seq("","","","FOO"))
//    assert(widgets.size === 1)
//    assert(widgets(0).size === 1)
//    assert(widgets(0)(0) === "FOO")
  }
  test("parseWidgets: multiple blank lines between widgets ignored") {
//    val widgets = ModelReader.parseWidgets(Seq("FOO","","","","","","BAR"))
//    assert(widgets.size === 2)
//    assert(widgets(0).size === 1)
//    assert(widgets(0)(0) === "FOO")
//    assert(widgets(1).size === 1)
//    assert(widgets(1)(0) === "BAR")
  }
  test("parseWidgets: empty model contains only view") {
//    val emptyModel = getResourceAsString("/system/empty.nlogo")
//    val map = ModelReader.parseModel(emptyModel)
//    val widgets = ModelReader.parseWidgets(map(ModelSection.Interface))
//    assert(widgets.size === 1)
//    assert(widgets(0)(0) === "GRAPHICS-WINDOW")
  }

  // strip/restoreLines
  test("StripSimple") {
//    assertResult("foo")(ModelReader.stripLines("foo"))
  }
  test("StripLineBreak") {
//    assertResult("foo\\nbar")(ModelReader.stripLines("foo\nbar"))
  }
  test("StripConsecutiveLineBreaks") {
//    assertResult("foo\\n\\nbar")(ModelReader.stripLines("foo\n\nbar"))
  }
  test("StripTrailingLineBreak") {
//    assertResult("foo\\n")(ModelReader.stripLines("foo\n"))
  }
  test("StripInnerNewLines") {
//    assertResult("foo\\n\\\"foo\\\\n\\\"")(
//      ModelReader.stripLines("foo\n\"foo\\n\""))
  }
  test("RestoreSimple") {
//    assertResult("foo")(ModelReader.restoreLines("foo"))
  }
  test("RestoreLineBreak") {
//    assertResult("foo\nbar")(ModelReader.restoreLines("foo\\nbar"))
  }
  test("RestoreConsecutiveLineBreaks") {
//    assertResult("foo\n\nbar")(ModelReader.restoreLines("foo\\n\\nbar"))
  }
  test("RestoreInitialLineBreaks") {
//    assertResult("\n\n\nfoo")(ModelReader.restoreLines("\\n\\n\\nfoo"))
  }
  test("RestoreInnerNewLines") {
//    assertResult("foo\n\"foo\\n\"")(
//      ModelReader.restoreLines("foo\\n\"foo\\\\n\""))
  }

}
