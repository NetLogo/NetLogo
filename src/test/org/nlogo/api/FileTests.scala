package org.nlogo.api

import org.scalatest.FunSuite

class FileTests extends FunSuite {
  test("StripSimple") {
    expect("foo")(File.stripLines("foo"))
  }
  test("StripLineBreak") {
    expect("foo\\nbar")(File.stripLines("foo\nbar"))
  }
  test("StripConsecutiveLineBreaks") {
    expect("foo\\n\\nbar")(File.stripLines("foo\n\nbar"))
  }
  test("StripTrailingLineBreak") {
    expect("foo\\n")(File.stripLines("foo\n"))
  }
  test("StripInnerNewLines") {
    expect("foo\\n\\\"foo\\\\n\\\"")(
      File.stripLines("foo\n\"foo\\n\""))
  }
  test("RestoreSimple") {
    expect("foo")(File.restoreLines("foo"))
  }
  test("RestoreLineBreak") {
    expect("foo\nbar")(File.restoreLines("foo\\nbar"))
  }
  test("RestoreConsecutiveLineBreaks") {
    expect("foo\n\nbar")(File.restoreLines("foo\\n\\nbar"))
  }
  test("RestoreInitialLineBreaks") {
    expect("\n\n\nfoo")(File.restoreLines("\\n\\n\\nfoo"))
  }
  test("RestoreInnerNewLines") {
    expect("foo\n\"foo\\n\"")(
      File.restoreLines("foo\\n\"foo\\\\n\""))
  }
}
