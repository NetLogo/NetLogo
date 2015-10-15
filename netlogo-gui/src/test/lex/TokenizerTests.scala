// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import org.scalatest.FunSuite
import org.nlogo.core.{ Token, TokenType }

class TokenizerTests extends FunSuite {
  val tokenizer = Tokenizer2D
  def tokenize(s: String) = {
    val result = tokenizer.tokenize(s, "")
    assertResult(TokenType.Eof)(result.last.tpe)
    result.toList.dropRight(1)
  }
  def tokenizeRobustly(s: String) = {
    val result = tokenizer.tokenizeRobustly(s)
    assertResult(TokenType.Eof)(result.last.tpe)
    result.toList.dropRight(1)
  }
  def firstBadToken(tokens: Seq[Token]) = tokens.find(_.tpe == TokenType.Bad)
  ///
  test("TokenizeSimpleExpr") {
    val expected = "Token(__ignore,Command,_ignore)" +
      "Token(round,Reporter,_round)" +
      "Token(0.5,Literal,0.5)"
    assertResult(expected)(
      tokenize("__ignore round 0.5").mkString)
  }
  test("TokenizeSimpleExprWithInitialWhitespace") {
    val tokens = tokenize("\n\n__ignore round 0.5")
    val expected =
      "Token(__ignore,Command,_ignore)" +
        "Token(round,Reporter,_round)" +
        "Token(0.5,Literal,0.5)"
    assertResult(expected)(tokens.mkString)
  }
  test("TokenizeSimpleExprWithInitialReturn") {
    val tokens = tokenize("\r__ignore round 0.5")
    val expected =
      "Token(__ignore,Command,_ignore)" +
        "Token(round,Reporter,_round)" +
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
      "Token(round,Reporter,_round)" +
        "Token(?,Ident,?)"
    assertResult(expected)(tokens.mkString)
  }
  test("TokenizeBreedOwn") {
    val tokens = tokenize("mice-own")
    val expected =
      "Token(mice-own,Keyword,MICE-OWN)"
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

  test("validIdentifier") {
    assert(tokenizer.isValidIdentifier("foo"))
  }
  test("invalidIdentifier1") {
    assert(!tokenizer.isValidIdentifier("foo bar"))
  }
  test("invalidIdentifier2") {
    assert(!tokenizer.isValidIdentifier("33"))
  }
  test("invalidIdentifier3") {
    assert(!tokenizer.isValidIdentifier("color"))
  }
  // check extension primitives
  test("extensionCommand") {
    val extensionManager = new org.nlogo.api.DummyExtensionManager {
      class DummyCommand extends org.nlogo.api.Command {
        def getAgentClassString = ""
        def getSyntax: org.nlogo.core.Syntax = null
        def getSwitchesBoolean = true
        def newInstance(name: String): org.nlogo.api.Command = null
        def perform(args: Array[org.nlogo.api.Argument], context: org.nlogo.api.Context) {}
      }
      override def anyExtensionsLoaded = true
      override def replaceIdentifier(name: String): org.nlogo.core.Primitive =
        if (name.equalsIgnoreCase("FOO")) new DummyCommand else null
    }
    assertResult("Token(foo,Ident,FOO)")(
      tokenizer.tokenizeForColorization("foo").mkString)
    assertResult("Token(foo,Command,FOO)")(
      tokenizer.tokenizeForColorization("foo", extensionManager).mkString)
  }
  // the method being tested here is used by the F1 key stuff - ST 1/23/08
  test("GetTokenAtPosition") {
    assertResult("Token(ask,Command,_ask:+0)")(
      tokenizer.getTokenAtPosition("ask turtles [set color blue]", 0).toString)
    assertResult("Token(ask,Command,_ask:+0)")(
      tokenizer.getTokenAtPosition("ask turtles [set color blue]", 1).toString)
    assertResult("Token(ask,Command,_ask:+0)")(
      tokenizer.getTokenAtPosition("ask turtles [set color blue]", 2).toString)
    assertResult("Token([,OpenBracket,null)")(
      tokenizer.getTokenAtPosition("ask turtles [set color blue]", 12).toString)
    assertResult("Token(set,Command,_set)")(
      tokenizer.getTokenAtPosition("ask turtles [set color blue]", 13).toString)
    assertResult("Token(set,Command,_set)")(
      tokenizer.getTokenAtPosition("ask turtles [set color blue]", 14).toString)
    assertResult("Token(blue,Literal,105.0)")(
      tokenizer.getTokenAtPosition("ask turtles [set color blue]", 24).toString)
  }
  // bug #88
  test("GetTokenAtPosition-bug88") {
    assertResult("Token(crt,Command,_createturtles:,+0)")(
      tokenizer.getTokenAtPosition("[crt", 1).toString)
  }
  // bug #139
  test("GetTokenAtPosition-bug139") {
    assertResult("Token(crt,Command,_createturtles:,+0)")(
      tokenizer.getTokenAtPosition("crt]", 3).toString)
    assertResult("Token(crt,Command,_createturtles:,+0)")(
      tokenizer.getTokenAtPosition("crt", 0).toString)
    assertResult("Token(crt,Command,_createturtles:,+0)")(
      tokenizer.getTokenAtPosition("crt", 3).toString)
  }
  // what about removed prims?
  test("RemovedPrims") {
    assertResult(TokenType.Ident)(
      tokenize("random-or-random-float").head.tpe)
    assertResult(TokenType.Ident)(
      tokenize("histogram-from").head.tpe)
    assertResult(TokenType.Reporter)(
      tokenizer.tokenizeAllowingRemovedPrims("random-or-random-float").head.tpe)
    assertResult(TokenType.Command)(
      tokenizer.tokenizeAllowingRemovedPrims("histogram-from").head.tpe)
  }
  // underscore stuff
  test("Empty1") {
    val tokens = tokenize("")
    assertResult("")(tokens.mkString)
  }
  test("Empty2") {
    val tokens = tokenize("\n")
    assertResult("")(tokens.mkString)
  }
  test("OneUnderscore1") {
    val tokens = tokenizeRobustly("_")
    // not really the right error message, but this whole magic-open thing is a kludge anyway
    assertResult("Token(_,Bad,This non-standard character is not allowed.)")(
      tokens.mkString)
  }
  test("OneUnderscore2") {
    val tokens = tokenizeRobustly("_\n")
    // not really the right error message, but this whole magic-open thing is a kludge anyway
    assertResult("Token(_,Bad,This non-standard character is not allowed.)")(
      tokens.mkString)
  }
  test("TwoUnderscores1") {
    val tokens = tokenizeRobustly("__")
    // not really the right error message, but this whole magic-open thing is a kludge anyway
    assertResult("Token(_,Bad,This non-standard character is not allowed.)" +
           "Token(_,Bad,This non-standard character is not allowed.)")(
      tokens.mkString)
  }
  test("TwoUnderscores2") {
    val tokens = tokenizeRobustly("__\n")
    // not really the right error message, but this whole magic-open thing is a kludge anyway
    assertResult("Token(_,Bad,This non-standard character is not allowed.)" +
           "Token(_,Bad,This non-standard character is not allowed.)")(
      tokens.mkString)
  }
  test("ThreeUnderscores1") {
    val tokens = tokenize("___")
    assertResult("Token(__magic-open,Command,_magicopen)")(
      tokens.mkString)
  }
  test("ThreeUnderscores2") {
    val tokens = tokenize("___\n")
    assertResult("Token(__magic-open,Command,_magicopen)")(
      tokens.mkString)
  }
  test("ThreeUnderscoresAndFoo1") {
    val tokens = tokenize("___foo")
    assertResult("Token(__magic-open,Command,_magicopen)" +
                 "Token(\"foo\",Literal,foo)")(
      tokens.mkString)
  }
  test("ThreeUnderscoresAndFoo2") {
    val tokens = tokenize("___foo\n")
    assertResult("Token(__magic-open,Command,_magicopen)" +
                 "Token(\"foo\",Literal,foo)")(
      tokens.mkString)
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
