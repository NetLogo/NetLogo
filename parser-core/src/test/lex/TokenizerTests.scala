// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import org.scalatest.FunSuite
import org.nlogo.core.{ SourceLocation, Token, TokenType, TestUtils },
  TestUtils.cleanJsNumbers

class TokenizerTests extends FunSuite {
  import Tokenizer.{ isValidIdentifier, getTokenAtPosition }

  def tokenize(s: String) = {
    val result = Tokenizer.tokenizeString(s, "").toSeq
    assertResult(TokenType.Eof)(result.last.tpe)
    result.dropRight(1)
  }
  def tokenizeSkippingWhitespace(s: String) = {
    val result = Tokenizer.tokenizeSkippingTrailingWhitespace(
      new java.io.StringReader(s), "").toSeq
    assertResult(TokenType.Eof)(result.last._1.tpe)
    result.dropRight(1)
  }
  def tokenizeWithWhitespace(s: String) = {
    val result = Tokenizer.tokenizeWithWhitespace(
      new java.io.StringReader(s), "").toSeq
    assertResult(TokenType.Eof)(result.last.tpe)
    result.dropRight(1)
  }
  def tokenizeRobustly(s: String) = {
    val result = Tokenizer.tokenize(new java.io.StringReader(s)).toList
    assertResult(TokenType.Eof)(result.last.tpe)
    result.dropRight(1)
  }
  def firstBadToken(tokens: Seq[Token]) =
    tokens.find(_.tpe == TokenType.Bad)
  ///
  test("TokenizeSimpleExpr") {
    val expected =
      "Token(__ignore,Ident,__IGNORE)" +
        "Token(round,Ident,ROUND)" +
        "Token(0.5,Literal,0.5)"
    assertResult(expected)(
      tokenize("__ignore round 0.5").mkString)
  }
  test("TokenizeSimpleExprWithInitialWhitespace") {
    val tokens = tokenize("\n\n__ignore round 0.5")
    val expected =
      "Token(__ignore,Ident,__IGNORE)" +
        "Token(round,Ident,ROUND)" +
        "Token(0.5,Literal,0.5)"
    assertResult(expected)(tokens.mkString)
  }
  test("TokenizeSimpleExprWithInitialReturn") {
    val tokens = tokenize("\r__ignore round 0.5")
    val expected =
      "Token(__ignore,Ident,__IGNORE)" +
        "Token(round,Ident,ROUND)" +
        "Token(0.5,Literal,0.5)"
    assertResult(expected)(tokens.mkString)
  }
  test("TokenizeQuestionMark") {
    val tokens = tokenize("round ?")
    val expected =
      "Token(round,Ident,ROUND)" +
        "Token(?,Ident,?)"
    assertResult(expected)(tokens.mkString)
  }
  test("ListOfLiterals") {
    val tokens = tokenize("[123 -456 \"a\"]")
    val expected = """|Token([,OpenBracket,null)
                      |Token(123,Literal,123.0)
                      |Token(-456,Literal,-456.0)
                      |Token("a",Literal,a)
                      |Token(],CloseBracket,null)""".stripMargin.replaceAll("\n", "")
    assertResult(cleanJsNumbers(expected))(cleanJsNumbers(tokens.mkString))

  }

  test("Empty1") {
    val tokens = tokenize("")
    assertResult("")(tokens.mkString)
  }

  test("Empty2") {
    val tokens = tokenize("\n")
    assertResult("")(tokens.mkString)
  }

  test("ListOfArrays") {
    val tokens = tokenize("[{{array: 0}} {{array: 1}}]")
    assertResult("Token([,OpenBracket,null)" +
                 "Token({{array: 0}},Extension,{{array: 0}})" +
                 "Token({{array: 1}},Extension,{{array: 1}})" +
                 "Token(],CloseBracket,null)")(
      tokens.mkString)
    assertResult(1)(tokens(1).start)
    assertResult(13)(tokens(1).end)
    assertResult(14)(tokens(2).start)
    assertResult(26)(tokens(2).end)
  }

  test("ArrayOfArrays") {
    val tokens = tokenize("{{array: 2: {{array: 0}} {{array: 1}}}}")
    val expected = "Token({{array: 2: {{array: 0}} {{array: 1}}}},Extension," +
      "{{array: 2: {{array: 0}} {{array: 1}}}})"
    assertResult(expected)(tokens.mkString)
  }

  testLexFailure("\"",           0, 1,  "Closing double quote is missing")
  testLexFailure("\"\\b\"",      0, 4,  "Illegal character after backslash")
  testLexFailure("\"\\\"",       0, 3,  "Closing double quote is missing")
  testLexFailure(""""abc""",     0, 4,  "Closing double quote is missing")
   // check that parser errors when string contains newline
  testLexFailure("\"abc\n\"",    0, 4,  "Closing double quote is missing")
  testLexFailure("1.2.3",        0, 5,  "Illegal number format")
  testLexFailure("{{array: 1: ", 0, 12, "End of file reached unexpectedly")
  testLexFailure("{{",           0, 2,  "End of file reached unexpectedly")
  testLexFailure("{{\n",         0, 3,  "End of line reached unexpectedly")
  testLexFailure("{{ {{ }}",     0, 8,  "End of file reached unexpectedly")
  // 216C is a Unicode character I chose pretty much at random.  it's a Roman numeral
  // for fifty, and *looks* just like an L, but is not a letter according to Unicode.
  testLexFailure("foo\u216Cbar", 3, 4,  "This non-standard character is not allowed.")
  testLexFailure("__ignore 3__ignore 4", 9, 18, "Illegal number format")

  test("carriageReturnsAreWhitespace") {
    val tokens = tokenize("a\rb")
    assertResult("Token(a,Ident,A)" + "Token(b,Ident,B)")(
      tokens.mkString)
  }

  testLexesSingleToken(".5",         ".5", TokenType.Literal, Double.box(0.5))
  testLexesSingleToken("-1",         "-1", TokenType.Literal, Double.box(-1.0))
  testLexesSingleToken("-.75",       "-.75", TokenType.Literal, Double.box(-0.75))
  testLexesSingleToken("foo",        "foo", TokenType.Ident, "FOO")
  testLexesSingleToken("_",          "_", TokenType.Ident, "_")
  testLexesSingleToken("\"foo\"",    "\"foo\"", TokenType.Literal, "foo")
  testLexesSingleToken("""""""",     "\"\"",TokenType.Literal,"")
  testLexesSingleToken(""""\"\""""", "\"\\\"\\\"\"",TokenType.Literal,"\"\"")
  testLexesSingleToken("\"\\\\\"",   "\"\\\\\"",TokenType.Literal,"\\")
  testLexesSingleToken("-.",         "-.", TokenType.Ident, "-.")
  testLexesSingleToken("-WOLF-SHAPE-00013", "-WOLF-SHAPE-00013", TokenType.Ident, "-WOLF-SHAPE-00013")

  /// Unicode
  test("unicode") {
    val o ="\u00F6"  // lower case o with umlaut
    val tokens = tokenize(o)
    assertResult("Token(" + o + ",Ident," + o.toUpperCase + ")")(
      tokens.mkString)
  }
  test("TokenizeOddCharactersInString") {
    val tokens = tokenize("\"foo\u216C\"")
    val expected = "Token(\"foo\u216C\",Literal,foo\u216C)"
    assertResult(expected)(tokens.mkString)
  }

  testWhitespace("no whitespace", "123", Seq("123"), Seq(0))
  testWhitespace("skips beginning whitespace", "    123", Seq("123"), Seq(4))
  testWhitespace("skips ending whitespcae", "123   ", Seq("123"), Seq(3))
  testWhitespace("skips beginning and end whitespace", "  123   ", Seq("123"), Seq(5))
  testWhitespace("skips whitespace on multiple tokens", "  123  456 ", Seq("123", "456"), Seq(4, 1))

  def testWhitespace(condition: String, text: String, expectedTexts: Seq[String], expectedSkips: Seq[Int]) {
    test(s"Tokenize with skip whitespace $condition") {
        val tokens = tokenizeSkippingWhitespace(text)
        assert(tokens.map(_._1.text) == expectedTexts)
        assert(tokens.map(_._2)      == expectedSkips)
    }
  }


  def testLexesSingleToken(tokenString: String, text: String, tpe: TokenType, value: AnyRef): Unit = {
    test(s"properly lexes $tokenString") {
      val token = tokenize(tokenString).head
      assertResult(text)(token.text)
      assertResult(tpe)(token.tpe)
      assertResult(cleanJsNumbers(value.toString))(token.value.toString)
    }
  }

  def testLexFailure(text: String, start: Int, end: Int, error: String): Unit =
    test(s"properly fails $text with message $error") {
      val tokens = tokenizeRobustly(text)
      val badToken = firstBadToken(tokens).getOrElse(
        throw new Exception(s"Expected bad token, got ${tokens.mkString}"))
      assert(start == badToken.start)
      assert(end   == badToken.end)
      assert(error == badToken.value)
    }

  test("checks valid identifiers") {
    Seq("abc", "a42", "------''''-------").foreach(ident => assert(isValidIdentifier(ident)))
  }

  test("checks invalid identifiers") {
    Seq("{{}}", "(", ";comment", "42", "\"abc\"", "```", "-- --").foreach(ident => assert(! isValidIdentifier(ident)))
  }

  test("gets token at a given position") {
    assert(getTokenAtPosition("", -1)   == None)
    assert(getTokenAtPosition("", 10)   == None)
    assert(getTokenAtPosition("t", 0)   == Some(Token("t", TokenType.Ident, "T")(SourceLocation(0, 1, ""))))
    assert(getTokenAtPosition("t s", 2) == Some(Token("s", TokenType.Ident, "S")(SourceLocation(2, 3, ""))))
    assert(getTokenAtPosition("abc def", 2) == Some(Token("abc", TokenType.Ident, "ABC")(SourceLocation(0, 3, ""))))
    assert(getTokenAtPosition("abc def", 4) == Some(Token("def", TokenType.Ident, "DEF")(SourceLocation(4, 7, ""))))
    assert(getTokenAtPosition("abc def ghi", 10) == Some(Token("ghi", TokenType.Ident, "GHI")(SourceLocation(8, 11, ""))))
  }

  test("prefers ident and keyword tokens to punctuation and literals") {
    assert(getTokenAtPosition("abc]", 3)    == Some(Token("abc", TokenType.Ident, "ABC")(SourceLocation(0, 3, ""))))
    assert(getTokenAtPosition("abc 123", 3) == Some(Token("abc", TokenType.Ident, "ABC")(SourceLocation(0, 3, ""))))
    assert(getTokenAtPosition("123 abc", 3) == Some(Token("abc", TokenType.Ident, "ABC")(SourceLocation(4, 7, ""))))
  }

  test("TokenizeWithWhitespaceCapturesWhitespace") {
    val tokens = tokenizeWithWhitespace("123   foo")
    assertResult(cleanJsNumbers("Token(123,Literal,123.0)"))(cleanJsNumbers(tokens.head.toString))
    assertResult("Token(   ,Whitespace,   )")(cleanJsNumbers(tokens(1).toString))
    assertResult("Token(foo,Ident,FOO)")(cleanJsNumbers(tokens(2).toString))
  }
}
