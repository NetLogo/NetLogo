// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import utest._
import org.nlogo.core.{ Token, TokenType }

object TokenizerTests extends TestSuite {
  def tests = TestSuite{

    def tokenize(s: String) = {
      val result = Tokenizer.tokenizeString(s, "").toSeq
      assert(TokenType.Eof == result.last.tpe)
      result.dropRight(1)
    }

    def tokenizeSkippingWhitespace(s: String) = {
      val result = Tokenizer.tokenizeSkippingTrailingWhitespace(
        new java.io.StringReader(s), "").toSeq
      assert(TokenType.Eof == result.last._1.tpe)
      result.dropRight(1)
    }

    def tokenizeRobustly(s: String) = {
      val result = Tokenizer.tokenizeString(s, "").toList
      assert(TokenType.Eof == result.last.tpe)
      result.dropRight(1)
    }

    def firstBadToken(tokens: Seq[Token]) =
      tokens.find(_.tpe == TokenType.Bad)

    "Empty1"-{
      assert(tokenize("").isEmpty)
    }

    "Empty2"-{
      assert(tokenize("\n").isEmpty)
    }

    "TokenizeIdent"-{
      val tokens = tokenize("foo")
      val expected = "Token(foo,Ident,FOO)"
      assert(expected == tokens.mkString)
    }

    "TokenizeQuestionMark"-{
      val tokens = tokenize("round ?")
      val expected =
        "Token(round,Ident,ROUND)" +
          "Token(?,Ident,?)"
      assert(expected == tokens.mkString)
    }

    "TokenizeUnderscore"-{
      val tokens = tokenize("_")
      assert("Token(_,Ident,_)" == tokens.mkString)
    }

    "TokenizeSimpleExpr"-{
      val expected = "Token(__ignore,Ident,__IGNORE)" +
      "Token(round,Ident,ROUND)" +
      "Token(0.5,Literal,0.5)"
      val actual = tokenize("__ignore round 0.5").mkString
      assert(expected == actual)
    }

    "TokenizeSimpleExprWithInitialWhitespace"-{
      val tokens = tokenize("\n\n__ignore round 0.5")
      val expected =
        "Token(__ignore,Ident,__IGNORE)" +
          "Token(round,Ident,ROUND)" +
          "Token(0.5,Literal,0.5)"
      assert(expected == tokens.mkString)
    }

    "TokenizeSimpleExprWithInitialReturn"-{
      val tokens = tokenize("\r__ignore round 0.5")
      val expected =
        "Token(__ignore,Ident,__IGNORE)" +
          "Token(round,Ident,ROUND)" +
          "Token(0.5,Literal,0.5)"
      assert(expected == tokens.mkString)
    }

    "TokenizeString"-{
      val tokens = tokenize("\"foo\"")
      val expected = "Token(\"foo\",Literal,foo)"
      assert(expected == tokens.mkString)
    }

    "TokenizeEmptyString"-{
      val tokens = tokenize("""""""")
      val expected = "Token(\"\",Literal,)"
      assert(expected == tokens.mkString)
    }

    "TokenizeStringOfEmptyString"-{
      val tokens = tokenize(""""\"\""""")
      val expected = "Token(\"\\\"\\\"\",Literal,\"\")"
      assert(expected == tokens.mkString)
    }

    "TokenizeUnknownEscape"-{
      val tokens = tokenizeRobustly("\"\\b\"")
      assert(0 == firstBadToken(tokens).get.start)
      assert(4 == firstBadToken(tokens).get.end)
      assert("Illegal character after backslash" ==
        firstBadToken(tokens).get.value)
    }

    "TokenizeUnclosedStringLiteral"-{
      val tokens = tokenizeRobustly(""""abc""")
      assert("Closing double quote is missing" == firstBadToken(tokens).get.value)
    }

    "TokenizeWeirdCaseWithBackSlash"-{
      val tokens = tokenizeRobustly("\"\\\"")
      assert(0 == firstBadToken(tokens).get.start)
      assert(3 == firstBadToken(tokens).get.end)
      assert("Closing double quote is missing" ==
        firstBadToken(tokens).get.value)
    }

    "TokenizeUnaryMinusNumber"-{
      val tokens = tokenize("-1")
      assert("Token(-1,Literal,-1)" == tokens.mkString)
    }

    "TokenizeLeadingDecimalNumber"-{
      val tokens = tokenize(".5")
      assert("Token(.5,Literal,0.5)" == tokens.mkString)
    }

    "TokenizeLeadingDecimalUnaryMinusNumber"-{
      val tokens = tokenize("-.75")
      assert("Token(-.75,Literal,-0.75)" == tokens.mkString)
    }

    "TokenizeBadNumberFormat1"-{
      val tokens = tokenizeRobustly("1.2.3")
      assert(0 == firstBadToken(tokens).get.start)
      assert(5 == firstBadToken(tokens).get.end)
      assert("Illegal number format" ==
        firstBadToken(tokens).get.value)
    }

    "TokenizeBadNumberFormat2"-{
      val tokens = tokenizeRobustly("__ignore 3__ignore 4")
      assert(9 == firstBadToken(tokens).get.start)
      assert(18 == firstBadToken(tokens).get.end)
      assert("Illegal number format" ==
        firstBadToken(tokens).get.value)
    }

    "TokenizeLooksLikePotentialNumber"-{
      val tokens = tokenize("-.")
      val expected = "Token(-.,Ident,-.)"
      assert(expected == tokens.mkString)
    }

    "unicode"-{
      val o ="\u00F6"  // lower case o with umlaut
      val tokens = tokenize(o)
      assert("Token(" + o + ",Ident," + o.toUpperCase + ")" ==
        tokens.mkString)
    }

    "TokenizeOddCharactersInString"-{
      val tokens = tokenize("\"foo\u216C\"")
      val expected = "Token(\"foo\u216C\",Literal,foo\u216C)"
      assert(expected == tokens.mkString)
    }

    "carriageReturnsAreWhitespace"-{
      val tokens = tokenize("a\rb")
      assert("Token(a,Ident,A)" + "Token(b,Ident,B)" ==
        tokens.mkString)
    }

    "UnclosedExtensionLiteral1"-{
      val tokens = tokenizeRobustly("{{")
      assert("Token(,Bad,End of file reached unexpectedly)" ==
        tokens.mkString)
    }

    "UnclosedExtensionLiteral2"-{
      val tokens = tokenizeRobustly("{{array: 1: ")
      assert("Token(,Bad,End of file reached unexpectedly)" ==
        tokens.mkString)
    }

    "UnclosedExtensionLiteral3"-{
      val tokens = tokenizeRobustly("{{\n")
      assert("Token(,Bad,End of line reached unexpectedly)" ==
        tokens.mkString)
    }

    "UnclosedExtensionLiteral4"-{
      val tokens = tokenizeRobustly("{{ {{ }}")
      assert("Token(,Bad,End of file reached unexpectedly)" ==
        tokens.mkString)
    }

    "SingleExtensionPrimitive"-{
      val tokens = tokenize("{{array: 0}}")
      assert("Token({{array: 0}},Extension,{{array: 0}})" ==
        tokens.mkString)
    }

    "ListOfArrays"-{
      val tokens = tokenize("[{{array: 0}} {{array: 1}}]")
      assert("Token([,OpenBracket,null)" +
        "Token({{array: 0}},Extension,{{array: 0}})" +
        "Token({{array: 1}},Extension,{{array: 1}})" +
        "Token(],CloseBracket,null)" ==
          tokens.mkString)
      assert(1 == tokens(1).start)
      assert(13 == tokens(1).end)
      assert(14 == tokens(2).start)
      assert(26 == tokens(2).end)
    }

    "ArrayOfArrays"-{
      val tokens = tokenize("{{array: 2: {{array: 0}} {{array: 1}}}}")
      val expected = "Token({{array: 2: {{array: 0}} {{array: 1}}}},Extension,{{array: 2: {{array: 0}} {{array: 1}}}})"
      assert(expected == tokens.mkString)
    }


    "TokenizeBadCharactersInIdent"-{
      // 216C is a Unicode character I chose pretty much at random.  it's a Roman numeral
      // for fifty, and *looks* just like an L, but is not a letter according to Unicode.
      val tokens = tokenizeRobustly("foo\u216Cbar")
      assert(3 == firstBadToken(tokens).get.start)
      assert(4 == firstBadToken(tokens).get.end)
      assert("This non-standard character is not allowed." ==
        firstBadToken(tokens).get.value)
    }

    "TokenizeWithSkipWhitespaceSkipsBeginningWhitespace"-{
      val tokens = tokenizeSkippingWhitespace("    123")
      assert("Token(123,Literal,123)" == tokens.head._1.toString)
      assert(4 == tokens.head._2)
    }

    "TokenizeWithSkipWhitespaceSkipsNoWhitespace"-{
      val tokens = tokenizeSkippingWhitespace("123")
      assert("Token(123,Literal,123)" == tokens.head._1.toString)
      assert(0 == tokens.head._2)
    }

    "TokenizeWithSkipWhitespaceSkipsEndingWhitespace"-{
      val tokens = tokenizeSkippingWhitespace("123   ")
      assert("Token(123,Literal,123)" == tokens.head._1.toString)
      assert(3 == tokens.head._2)
    }

    "TokenizeWithSkipWhitespaceSkipsBeginningAndEndWhitespace"-{
      val tokens = tokenizeSkippingWhitespace("  123   ")
      assert("Token(123,Literal,123)" == tokens.head._1.toString)
      assert(5 == tokens.head._2)
    }

    "TokenizeWithSkipWhitespaceOnMultipleTokens"-{
      val tokens = tokenizeSkippingWhitespace("  123  456 ")
      assert("Token(123,Literal,123)" == tokens(0)._1.toString)
      assert(4 == tokens(0)._2)
      assert("Token(456,Literal,456)" == tokens(1)._1.toString)
      assert(1 == tokens(1)._2)
    }
  }
}
