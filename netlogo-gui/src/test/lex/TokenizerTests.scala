// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import org.scalatest.FunSuite
import org.nlogo.api.{ Token, TokenType }

class TokenizerTests extends FunSuite {
  val tokenizer = Tokenizer2D
  def tokenize(s: String) = {
    val result = tokenizer.tokenize(s, "")
    assertResult(TokenType.EOF)(result.last.tyype)
    result.toList.dropRight(1)
  }
  def tokenizeRobustly(s: String) = {
    val result = tokenizer.tokenizeRobustly(s)
    assertResult(TokenType.EOF)(result.last.tyype)
    result.toList.dropRight(1)
  }
  def firstBadToken(tokens: Seq[Token]) = tokens.find(_.tyype == TokenType.BAD)
  ///
  test("TokenizeSimpleExpr") {
    val expected = "Token(__ignore,COMMAND,_ignore)" +
      "Token(round,REPORTER,_round)" +
      "Token(0.5,CONSTANT,0.5)"
    assertResult(expected)(
      tokenize("__ignore round 0.5").mkString)
  }
  test("TokenizeSimpleExprWithInitialWhitespace") {
    val tokens = tokenize("\n\n__ignore round 0.5")
    val expected =
      "Token(__ignore,COMMAND,_ignore)" +
        "Token(round,REPORTER,_round)" +
        "Token(0.5,CONSTANT,0.5)"
    assertResult(expected)(tokens.mkString)
  }
  test("TokenizeSimpleExprWithInitialReturn") {
    val tokens = tokenize("\r__ignore round 0.5")
    val expected =
      "Token(__ignore,COMMAND,_ignore)" +
        "Token(round,REPORTER,_round)" +
        "Token(0.5,CONSTANT,0.5)"
    assertResult(expected)(tokens.mkString)
  }
  test("TokenizeIdent") {
    val tokens = tokenize("foo")
    val expected = "Token(foo,IDENT,FOO)"
    assertResult(expected)(tokens.mkString)
  }
  test("TokenizeQuestionMark") {
    val tokens = tokenize("round ?")
    val expected =
      "Token(round,REPORTER,_round)" +
        "Token(?,IDENT,?)"
    assertResult(expected)(tokens.mkString)
  }
  test("TokenizeBreedOwn") {
    val tokens = tokenize("mice-own")
    val expected =
      "Token(mice-own,KEYWORD,MICE-OWN)"
    assertResult(expected)(tokens.mkString)
  }
  test("TokenizeUnknownEscape") {
    val tokens = tokenizeRobustly("\"\\b\"")
    assertResult(0)(firstBadToken(tokens).get.startPos)
    assertResult(4)(firstBadToken(tokens).get.endPos)
    assertResult("Illegal character after backslash")(
      firstBadToken(tokens).get.value)
  }
  test("TokenizeWeirdCaseWithBackSlash") {
    val tokens = tokenizeRobustly("\"\\\"")
    assertResult(0)(firstBadToken(tokens).get.startPos)
    assertResult(3)(firstBadToken(tokens).get.endPos)
    assertResult("Closing double quote is missing")(
      firstBadToken(tokens).get.value)
  }
  test("TokenizeBadNumberFormat1") {
    val tokens = tokenizeRobustly("1.2.3")
    assertResult(0)(firstBadToken(tokens).get.startPos)
    assertResult(5)(firstBadToken(tokens).get.endPos)
    assertResult("Illegal number format")(
      firstBadToken(tokens).get.value)
  }
  test("TokenizeBadNumberFormat2") {
    val tokens = tokenizeRobustly("__ignore 3__ignore 4")
    assertResult(9)(firstBadToken(tokens).get.startPos)
    assertResult(18)(firstBadToken(tokens).get.endPos)
    assertResult("Illegal number format")(
      firstBadToken(tokens).get.value)
  }
  test("TokenizeLooksLikePotentialNumber") {
    val tokens = tokenize("-.")
    val expected = "Token(-.,IDENT,-.)"
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
        def getSyntax: org.nlogo.api.Syntax = null
        def getSwitchesBoolean = true
        def newInstance(name: String): org.nlogo.api.Command = null
        def perform(args: Array[org.nlogo.api.Argument], context: org.nlogo.api.Context) {}
      }
      override def anyExtensionsLoaded = true
      override def replaceIdentifier(name: String): org.nlogo.api.Primitive =
        if (name.equalsIgnoreCase("FOO")) new DummyCommand else null
    }
    assertResult("Token(foo,IDENT,FOO)")(
      tokenizer.tokenizeForColorization("foo").mkString)
    assertResult("Token(foo,COMMAND,FOO)")(
      tokenizer.tokenizeForColorization("foo", extensionManager).mkString)
  }
  // the method being tested here is used by the F1 key stuff - ST 1/23/08
  test("GetTokenAtPosition") {
    assertResult("Token(ask,COMMAND,_ask:+0)")(
      tokenizer.getTokenAtPosition("ask turtles [set color blue]", 0).toString)
    assertResult("Token(ask,COMMAND,_ask:+0)")(
      tokenizer.getTokenAtPosition("ask turtles [set color blue]", 1).toString)
    assertResult("Token(ask,COMMAND,_ask:+0)")(
      tokenizer.getTokenAtPosition("ask turtles [set color blue]", 2).toString)
    assertResult("Token([,OPEN_BRACKET,null)")(
      tokenizer.getTokenAtPosition("ask turtles [set color blue]", 12).toString)
    assertResult("Token(set,COMMAND,_set)")(
      tokenizer.getTokenAtPosition("ask turtles [set color blue]", 13).toString)
    assertResult("Token(set,COMMAND,_set)")(
      tokenizer.getTokenAtPosition("ask turtles [set color blue]", 14).toString)
    assertResult("Token(blue,CONSTANT,105.0)")(
      tokenizer.getTokenAtPosition("ask turtles [set color blue]", 24).toString)
  }
  // bug #88
  test("GetTokenAtPosition-bug88") {
    assertResult("Token(crt,COMMAND,_createturtles:,+0)")(
      tokenizer.getTokenAtPosition("[crt", 1).toString)
  }
  // bug #139
  test("GetTokenAtPosition-bug139") {
    assertResult("Token(crt,COMMAND,_createturtles:,+0)")(
      tokenizer.getTokenAtPosition("crt]", 3).toString)
    assertResult("Token(crt,COMMAND,_createturtles:,+0)")(
      tokenizer.getTokenAtPosition("crt", 0).toString)
    assertResult("Token(crt,COMMAND,_createturtles:,+0)")(
      tokenizer.getTokenAtPosition("crt", 3).toString)
  }
  // what about removed prims?
  test("RemovedPrims") {
    assertResult(TokenType.IDENT)(
      tokenize("random-or-random-float").head.tyype)
    assertResult(TokenType.IDENT)(
      tokenize("histogram-from").head.tyype)
    assertResult(TokenType.REPORTER)(
      tokenizer.tokenizeAllowingRemovedPrims("random-or-random-float").head.tyype)
    assertResult(TokenType.COMMAND)(
      tokenizer.tokenizeAllowingRemovedPrims("histogram-from").head.tyype)
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
    assertResult("Token(_,BAD,This non-standard character is not allowed.)")(
      tokens.mkString)
  }
  test("OneUnderscore2") {
    val tokens = tokenizeRobustly("_\n")
    // not really the right error message, but this whole magic-open thing is a kludge anyway
    assertResult("Token(_,BAD,This non-standard character is not allowed.)")(
      tokens.mkString)
  }
  test("TwoUnderscores1") {
    val tokens = tokenizeRobustly("__")
    // not really the right error message, but this whole magic-open thing is a kludge anyway
    assertResult("Token(_,BAD,This non-standard character is not allowed.)" +
           "Token(_,BAD,This non-standard character is not allowed.)")(
      tokens.mkString)
  }
  test("TwoUnderscores2") {
    val tokens = tokenizeRobustly("__\n")
    // not really the right error message, but this whole magic-open thing is a kludge anyway
    assertResult("Token(_,BAD,This non-standard character is not allowed.)" +
           "Token(_,BAD,This non-standard character is not allowed.)")(
      tokens.mkString)
  }
  test("ThreeUnderscores1") {
    val tokens = tokenize("___")
    assertResult("Token(__magic-open,COMMAND,_magicopen)")(
      tokens.mkString)
  }
  test("ThreeUnderscores2") {
    val tokens = tokenize("___\n")
    assertResult("Token(__magic-open,COMMAND,_magicopen)")(
      tokens.mkString)
  }
  test("ThreeUnderscoresAndFoo1") {
    val tokens = tokenize("___foo")
    assertResult("Token(__magic-open,COMMAND,_magicopen)" +
                 "Token(\"foo\",CONSTANT,foo)")(
      tokens.mkString)
  }
  test("ThreeUnderscoresAndFoo2") {
    val tokens = tokenize("___foo\n")
    assertResult("Token(__magic-open,COMMAND,_magicopen)" +
                 "Token(\"foo\",CONSTANT,foo)")(
      tokens.mkString)
  }
  test("ListOfArrays") {
    val tokens = tokenize("[{{array: 0}} {{array: 1}}]")
    assertResult("Token([,OPEN_BRACKET,null)" +
                 "Token({{array: 0}},LITERAL,{{array: 0}})" +
                 "Token({{array: 1}},LITERAL,{{array: 1}})" +
                 "Token(],CLOSE_BRACKET,null)")(
      tokens.mkString)
    assertResult(1)(tokens(1).startPos)
    assertResult(13)(tokens(1).endPos)
    assertResult(14)(tokens(2).startPos)
    assertResult(26)(tokens(2).endPos)
  }

  test("ArrayOfArrays") {
    val tokens = tokenize("{{array: 2: {{array: 0}} {{array: 1}}}}")
    assertResult("Token({{array: 2: {{array: 0}} {{array: 1}}}},LITERAL,{{array: 2: {{array: 0}} {{array: 1}}}})")(
      tokens.mkString)
  }

  test("UnclosedExtensionLiteral1") {
    val tokens = tokenizeRobustly("{{array: 1: ")
    assertResult("Token(,BAD,End of file reached unexpectedly)")(
      tokens.mkString)
  }
  test("UnclosedExtensionLiteral2") {
    val tokens = tokenizeRobustly("{{")
    assertResult("Token(,BAD,End of file reached unexpectedly)")(
      tokens.mkString)
  }
  test("UnclosedExtensionLiteral3") {
    val tokens = tokenizeRobustly("{{\n")
    assertResult("Token(,BAD,End of line reached unexpectedly)")(
      tokens.mkString)
  }

  test("carriageReturnsAreWhitespace") {
    val tokens = tokenize("a\rb")
    assertResult("Token(a,IDENT,A)" + "Token(b,IDENT,B)")(
      tokens.mkString)
  }

  /// Unicode
  test("unicode") {
    val o ="\u00F6"  // lower case o with umlaut
    val tokens = tokenize(o)
    assertResult("Token(" + o + ",IDENT," + o.toUpperCase + ")")(
      tokens.mkString)
  }
  test("TokenizeBadCharactersInIdent") {
    // 216C is a Unicode character I chose pretty much at random.  it's a Roman numeral
    // for fifty, and *looks* just like an L, but is not a letter according to Unicode.
    val tokens = tokenizeRobustly("foo\u216Cbar")
    assertResult(3)(firstBadToken(tokens).get.startPos)
    assertResult(4)(firstBadToken(tokens).get.endPos)
    assertResult("This non-standard character is not allowed.")(
      firstBadToken(tokens).get.value)
  }
  test("TokenizeOddCharactersInString") {
    val tokens = tokenize("\"foo\u216C\"")
    val expected = "Token(\"foo\u216C\",CONSTANT,foo\u216C)"
    assertResult(expected)(tokens.mkString)
  }

}
