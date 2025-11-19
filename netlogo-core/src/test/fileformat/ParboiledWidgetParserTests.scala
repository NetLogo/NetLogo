// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.util.AnyFunSuiteEx

class ParboiledWidgetParserTests extends AnyFunSuiteEx {
  // strip/restoreLines
  test("StripSimple") {
    assertResult("foo")(ParsingStringUtils.stripLines("foo"))
  }
  test("StripLineBreak") {
    assertResult("foo\\nbar")(ParsingStringUtils.stripLines("foo\nbar"))
  }
  test("StripConsecutiveLineBreaks") {
    assertResult("foo\\n\\nbar")(ParsingStringUtils.stripLines("foo\n\nbar"))
  }
  test("StripTrailingLineBreak") {
    assertResult("foo\\n")(ParsingStringUtils.stripLines("foo\n"))
  }
  test("StripInnerNewLines") {
    assertResult("foo\\n\\\"foo\\\\n\\\"")(
      ParsingStringUtils.stripLines("foo\n\"foo\\n\""))
  }
  test("RestoreSimple") {
    assertResult("foo")(ParsingStringUtils.restoreLines("foo"))
  }
  test("RestoreLineBreak") {
    assertResult("foo\nbar")(ParsingStringUtils.restoreLines("foo\\nbar"))
  }
  test("RestoreConsecutiveLineBreaks") {
    assertResult("foo\n\nbar")(ParsingStringUtils.restoreLines("foo\\n\\nbar"))
  }
  test("RestoreInitialLineBreaks") {
    assertResult("\n\n\nfoo")(ParsingStringUtils.restoreLines("\\n\\n\\nfoo"))
  }
  test("RestoreInnerNewLines") {
    assertResult("foo\n\"foo\\n\"")(
      ParsingStringUtils.restoreLines("foo\\n\"foo\\\\n\""))
  }
}
