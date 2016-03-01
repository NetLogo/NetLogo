// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.scalatest.FunSuite

class InfoConverterTests extends FunSuite {
  import InfoConverter.convert
  test("oneSection") {
    assertResult("## WHAT IS IT?\n\nWho knows?")(
      convert("WHAT IS IT?\n-----------\nWho knows?"))
  }
  test("twoSections") {
    assertResult("## WHAT IS IT?\n\n" +
           "Who knows?\n\n" +
           "## HOW IT WORKS\n\n" +
           "Who cares?")(
      convert("WHAT IS IT?\n-----------\nWho knows?\n\nHOW IT WORKS\n------------\nWho cares?"))
  }
  test("oneParagraphWithLineBreak") {
    assertResult("foo  \nbar")(convert("foo\nbar"))
  }
  test("twoParagraphs") {
    assertResult("foo\n\nbar")(convert("foo\n\nbar"))
  }
  test("condense blank lines") {
    assertResult("foo\n\nbar")(convert("foo\n\n\nbar"))
  }
  test("preformatted") {
    assertResult("foo\n\n    bar\n     baz\n      |qux\n\ndone")(
      convert("foo\n|bar\n| baz\n|  |qux\ndone"))
  }
  test("preformattedOneLine") {
    assertResult("\n    bar\n")(
      convert("|bar"))
  }
  test("preformattedTwoLines") {
    assertResult("\n    foo\n    bar\n")(
      convert("|foo\n|bar"))
  }
  test("preformattedBeforeSectionHeader") {
    assertResult("\n    foo\n\n## BAR\n\nbar")(
      convert("|foo\n\nBAR\n---\nbar"))
  }
  test("allCapsIsHeaderEvenWithoutDashes") {
    assertResult("## FOO\n")(convert("FOO"))
  }
  test("allCapsIsntHeaderWhenPrecededByNonBlankLine") {
    assertResult("bar  \nFOO")(convert("bar\nFOO"))
  }
}
