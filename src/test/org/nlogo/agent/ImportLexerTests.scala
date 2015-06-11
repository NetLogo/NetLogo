// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.scalatest.FunSuite

class ImportLexerTests extends FunSuite {
  def testSimple(source: String, expectedFields: List[String]) {
    assertResult(expectedFields)(ImportLexer.lex(source).toList)
  }
  def testInvalid(source: String, messageStart: String) {
    val e = intercept[ImportLexer.LexerException] {
      ImportLexer.lex(source)
    }
    assert(e.getMessage.startsWith(messageStart), e.getMessage)
  }
  ///
  test("SimpleLine1") {
    testSimple("foo,bar,baz", List("foo", "bar", "baz"))
  }
  test("SimpleLine2") {
    testSimple("foo, bar, baz", List("foo", "bar", "baz"))
  }
  test("ConsecutiveCommas1") {
    testSimple(",", List("", ""))
  }
  test("ConsecutiveCommas2") {
    testSimple(", ", List("", ""))
  }
  test("ConsecutiveCommas3") {
    testSimple("foo,,,baz", List("foo", "", "", "baz"))
  }
  test("ConsecutiveCommas4") {
    testSimple("foo,   ,     ,baz", List("foo", "", "", "baz"))
  }
  test("BlankLine1") {
    testSimple(" ", List(""))
  }
  test("BlankLine2") {
    testSimple("         ", List(""))
  }
  test("BlankLine3") {
    testSimple("", List(""))
  }
  test("FieldsWithSpaces") {
    testSimple("foo, bar fish goo , baz", List("foo", "bar fish goo", "baz"))
  }
  test("OneEmptyQuotedField") {
    testSimple("\"\"", List(""))
  }
  test("QuotedFields") {
    testSimple("\"foo\",\"bar\",\"baz\"", List("foo", "bar", "baz"))
  }
  test("TrailingSpaces1") {
    testSimple("\"foo\",\"bar\",\"baz\" ", List("foo", "bar", "baz"))
  }
  test("TrailingSpaces2") {
    testSimple("\"foo\",\"bar\",baz ", List("foo", "bar", "baz"))
  }
  test("QuotedFieldsWithSpaces") {
    testSimple(" \" foo foobar \" , \"   bar barfoo  \" , \"b a 6 z\"",
      List(" foo foobar ", "   bar barfoo  ", "b a 6 z"))
  }
  test("CommasInQuotes") {
    testSimple("\",\" , \",,\",\" , \"", List(",", ",,", " , "))
  }
  test("InvalidQuotedFields1") {
    testInvalid("foo, \"bar fish goo\" asd, baz",
      "Quoted fields must be followed by comma or end of line")
  }
  test("InvalidQuotedFields2") {
    testInvalid("foo, \"bar fish goo\"asd, baz",
      "Quoted fields must be followed by comma or end of line")
  }
  test("UnclosedQuote1") {
    testInvalid("\"", "Unclosed double quote")
  }
  test("UnclosedQuote2") {
    testInvalid("foo, \"bar", "Unclosed double quote")
  }
  test("EscapedQuotes1") {
    testSimple("foo, \"\"\"bar\"\"\", baz",
      List("foo", "\"bar\"", "baz"))
  }
  test("EscapedQuotes2") {
    testSimple("foo, \"\"\"\t\\\"\"bar\"\"\", baz",
      List("foo", "\"\t\\\"bar\"", "baz"))
  }
  test("UnclosedQuoteWithEscapes") {
    testInvalid("\"\"\"", "Quoted fields must be followed by comma or end of line")
  }
}
