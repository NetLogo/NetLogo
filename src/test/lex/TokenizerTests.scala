// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import org.scalatest.FunSuite
import org.nlogo.api, api.{ Token, TokenType }

class TokenizerTests extends FunSuite {

  def tokenize(s: String) = {
    val result = Tokenizer.tokenize(s, "").toSeq
    assertResult(TokenType.Eof)(result.last.tpe)
    result.dropRight(1)
  }
  def tokenizeRobustly(s: String) = {
    val result = Tokenizer.tokenizeRobustly(new java.io.StringReader(s)).toList
    assertResult(TokenType.Eof)(result.last.tpe)
    result.dropRight(1)
  }
  def firstBadToken(tokens: Seq[Token]) =
    tokens.find(_.tpe == TokenType.Bad)
  ///
  test("TokenizeSimpleExpr") {
    val expected = "Token(__ignore,Ident,__IGNORE)" +
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
  test("TokenizeIdent") {
    val tokens = tokenize("foo")
    val expected = "Token(foo,Ident,FOO)"
    assertResult(expected)(tokens.mkString)
  }
  test("TokenizeQuestionMark") {
    val tokens = tokenize("round ?")
    val expected =
      "Token(round,Ident,ROUND)" +
        "Token(?,Ident,?)"
    assertResult(expected)(tokens.mkString)
  }
  test("TokenizeUnknownEscape") {
    val tokens = tokenizeRobustly("\"\\b\"")
    assertResult(0)(firstBadToken(tokens).get.start)
    assertResult(4)(firstBadToken(tokens).get.end)
    assertResult("Illegal character after backslash")(
      firstBadToken(tokens).get.value)
  }
  test("TokenizeWeirdCaseWithBackSlash") {
    val tokens = tokenizeRobustly("\"\\\"")
    assertResult(0)(firstBadToken(tokens).get.start)
    assertResult(3)(firstBadToken(tokens).get.end)
    assertResult("Closing double quote is missing")(
      firstBadToken(tokens).get.value)
  }
  test("TokenizeBadNumberFormat1") {
    val tokens = tokenizeRobustly("1.2.3")
    assertResult(0)(firstBadToken(tokens).get.start)
    assertResult(5)(firstBadToken(tokens).get.end)
    assertResult("Illegal number format")(
      firstBadToken(tokens).get.value)
  }
  test("TokenizeBadNumberFormat2") {
    val tokens = tokenizeRobustly("__ignore 3__ignore 4")
    assertResult(9)(firstBadToken(tokens).get.start)
    assertResult(18)(firstBadToken(tokens).get.end)
    assertResult("Illegal number format")(
      firstBadToken(tokens).get.value)
  }
  test("TokenizeLooksLikePotentialNumber") {
    val tokens = tokenize("-.")
    val expected = "Token(-.,Ident,-.)"
    assertResult(expected)(tokens.mkString)
  }

  test("Empty1") {
    val tokens = tokenize("")
    assertResult("")(tokens.mkString)
  }
  test("Empty2") {
    val tokens = tokenize("\n")
    assertResult("")(tokens.mkString)
  }
  test("underscore") {
    val tokens = tokenize("_")
    assertResult("Token(_,Ident,_)")(tokens.mkString)
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
    assertResult("Token({{array: 2: {{array: 0}} {{array: 1}}}},Extension,{{array: 2: {{array: 0}} {{array: 1}}}})")(
      tokens.mkString)
  }

  test("UnclosedExtensionLiteral1") {
    val tokens = tokenizeRobustly("{{array: 1: ")
    assertResult("Token(,Bad,End of file reached unexpectedly)")(
      tokens.mkString)
  }
  test("UnclosedExtensionLiteral2") {
    val tokens = tokenizeRobustly("{{")
    assertResult("Token(,Bad,End of file reached unexpectedly)")(
      tokens.mkString)
  }
  test("UnclosedExtensionLiteral3") {
    val tokens = tokenizeRobustly("{{\n")
    assertResult("Token(,Bad,End of line reached unexpectedly)")(
      tokens.mkString)
  }

  test("carriageReturnsAreWhitespace") {
    val tokens = tokenize("a\rb")
    assertResult("Token(a,Ident,A)" + "Token(b,Ident,B)")(
      tokens.mkString)
  }

  /// Unicode
  test("unicode") {
    val o ="\u00F6"  // lower case o with umlaut
    val tokens = tokenize(o)
    assertResult("Token(" + o + ",Ident," + o.toUpperCase + ")")(
      tokens.mkString)
  }
  test("TokenizeBadCharactersInIdent") {
    // 216C is a Unicode character I chose pretty much at random.  it's a Roman numeral
    // for fifty, and *looks* just like an L, but is not a letter according to Unicode.
    val tokens = tokenizeRobustly("foo\u216Cbar")
    assertResult(3)(firstBadToken(tokens).get.start)
    assertResult(4)(firstBadToken(tokens).get.end)
    assertResult("This non-standard character is not allowed.")(
      firstBadToken(tokens).get.value)
  }
  test("TokenizeOddCharactersInString") {
    val tokens = tokenize("\"foo\u216C\"")
    val expected = "Token(\"foo\u216C\",Literal,foo\u216C)"
    assertResult(expected)(tokens.mkString)
  }

}
