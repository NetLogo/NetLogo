// (C) 2011 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.scalatest.FunSuite

class InfoConverterTests extends FunSuite {
  import InfoConverter.convert
  test("oneSection") {
    expect("## WHAT IS IT?\n\nWho knows?")(
      convert("WHAT IS IT?\n-----------\nWho knows?"))
  }
  test("twoSections") {
    expect("## WHAT IS IT?\n\n" +
           "Who knows?\n\n" +
           "## HOW IT WORKS\n\n" +
           "Who cares?")(
      convert("WHAT IS IT?\n-----------\nWho knows?\n\nHOW IT WORKS\n------------\nWho cares?"))
  }
  test("oneParagraphWithLineBreak") {
    expect("foo  \nbar")(convert("foo\nbar"))
  }
  test("twoParagraphs") {
    expect("foo\n\nbar")(convert("foo\n\nbar"))
  }
  test("condense blank lines") {
    expect("foo\n\nbar")(convert("foo\n\n\nbar"))
  }
  test("preformatted") {
    expect("foo\n\n    bar\n     baz\n      |qux\n\ndone")(
      convert("foo\n|bar\n| baz\n|  |qux\ndone"))
  }
  test("preformattedOneLine") {
    expect("\n    bar\n")(
      convert("|bar"))
  }
  test("preformattedTwoLines") {
    expect("\n    foo\n    bar\n")(
      convert("|foo\n|bar"))
  }
  test("preformattedBeforeSectionHeader") {
    expect("\n    foo\n\n## BAR\n\nbar")(
      convert("|foo\n\nBAR\n---\nbar"))
  }
  test("allCapsIsHeaderEvenWithoutDashes") {
    expect("## FOO\n")(convert("FOO"))
  }
  test("allCapsIsntHeaderWhenPrecededByNonBlankLine") {
    expect("bar  \nFOO")(convert("bar\nFOO"))
  }
}
