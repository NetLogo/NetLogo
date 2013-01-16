// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.scalatest.FunSuite

class InfoConverterTests extends FunSuite {
  import InfoConverter.convert
  test("oneSection") {
    expectResult("## WHAT IS IT?\n\nWho knows?")(
      convert("WHAT IS IT?\n-----------\nWho knows?"))
  }
  test("twoSections") {
    expectResult("## WHAT IS IT?\n\n" +
           "Who knows?\n\n" +
           "## HOW IT WORKS\n\n" +
           "Who cares?")(
      convert("WHAT IS IT?\n-----------\nWho knows?\n\nHOW IT WORKS\n------------\nWho cares?"))
  }
  test("oneParagraphWithLineBreak") {
    expectResult("foo  \nbar")(convert("foo\nbar"))
  }
  test("twoParagraphs") {
    expectResult("foo\n\nbar")(convert("foo\n\nbar"))
  }
  test("condense blank lines") {
    expectResult("foo\n\nbar")(convert("foo\n\n\nbar"))
  }
  test("preformatted") {
    expectResult("foo\n\n    bar\n     baz\n      |qux\n\ndone")(
      convert("foo\n|bar\n| baz\n|  |qux\ndone"))
  }
  test("preformattedOneLine") {
    expectResult("\n    bar\n")(
      convert("|bar"))
  }
  test("preformattedTwoLines") {
    expectResult("\n    foo\n    bar\n")(
      convert("|foo\n|bar"))
  }
  test("preformattedBeforeSectionHeader") {
    expectResult("\n    foo\n\n## BAR\n\nbar")(
      convert("|foo\n\nBAR\n---\nbar"))
  }
  test("allCapsIsHeaderEvenWithoutDashes") {
    expectResult("## FOO\n")(convert("FOO"))
  }
  test("allCapsIsntHeaderWhenPrecededByNonBlankLine") {
    expectResult("bar  \nFOO")(convert("bar\nFOO"))
  }
}
