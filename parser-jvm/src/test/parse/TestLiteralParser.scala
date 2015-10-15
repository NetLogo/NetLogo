// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.scalatest.FunSuite

import org.nlogo.core.{Dump, CompilerException, LogoList, Token, LiteralImportHandler, StringEscaper},
  LiteralImportHandler.Parser

class TestLiteralParser extends FunSuite {

  def importHandler(isImport: Boolean) =
    if (isImport)
      new LiteralImportHandler {
        override def parseExtensionLiteral(token: Token): AnyRef = {
          assert(token.value == "{{foo: bar baz}}")
          "PARSEDEXTENSIONOBJECT"
        }
        override def parseLiteralAgentOrAgentSet(tokens: Iterator[Token], parser: Parser): AnyRef = {
          assert(tokens.next().text == "agent-parseable")
          tokens.next()
          "PARSEDAGENTLITERAL"
        }
      }
    else
      new LiteralImportHandler {
        override def parseExtensionLiteral(token: Token): AnyRef =
          throw new CompilerException(
            "Can only have literal agents and agentsets if importing.",
            token.start, token.end, token.filename)
        override def parseLiteralAgentOrAgentSet(tokens: Iterator[Token], parser: Parser): AnyRef = {
          val token = tokens.next()
          throw new CompilerException(
            "Can only have literal agents and agentsets if importing.",
            token.start, token.end, token.filename)
        }
      }

  def tokenizeString(input: String): Iterator[Token] =
    FrontEnd.tokenizer.tokenizeString(input).map(Namer0)

  def toLiteral(input: String,
                isImport: Boolean = true): AnyRef = {
    val literalParser = new LiteralParser(importHandler(isImport))
    literalParser.getLiteralValue(tokenizeString(input))
  }

  def toLiteralList(input: String, isImport: Boolean = true): LogoList = {
    val tokens = FrontEnd.tokenizer.tokenizeString(input).map(Namer0)
    val literalParser = new LiteralParser(importHandler(isImport))
    val (result, _) = literalParser.parseLiteralList(tokens.next(), tokens)
    result
  }

  def testError(input: String, error: String, isImport: Boolean = true) {
    val e = intercept[CompilerException] {
      toLiteral(input, isImport)
    }
    assertResult(error)(e.getMessage)
  }

  def dumpObject(obj: AnyRef): String = Dump.logoObject(obj)

  test("booleanTrue") { assertResult(java.lang.Boolean.TRUE)(toLiteral("true")) }
  test("booleanFalse") { assertResult(java.lang.Boolean.FALSE)(toLiteral("false")) }
  test("literalInt") { assertResult(Double.box(4))(toLiteral("4")) }
  test("literalIntWhitespace") { assertResult(Double.box(4))(toLiteral("  4\t")) }
  test("literalIntParens") { assertResult(Double.box(4))(toLiteral(" (4)\t")) }
  test("literalIntParens2") { assertResult(Double.box(4))(toLiteral(" ((4)\t)")) }
  test("literalIntBadParens") { testError("((4)", "Expected a closing parenthesis.") }
  test("literalIntBadParens2") { testError("((4)))", "Extra characters after literal.") }
  test("largeLiteral1") { testError("9999999999999999999999999999999999999999999999999", "Illegal number format") }
  test("largeLiteral2") { testError("-9999999999999999999999999999999999999999999999999", "Illegal number format") }
  test("largeLiteral3") { testError("9007199254740993", "9007199254740993 is too large to be represented exactly as an integer in NetLogo") }
  test("largeLiteral4") { testError("-9007199254740993", "-9007199254740993 is too large to be represented exactly as an integer in NetLogo") }
  test("literalString") { assertResult("hi there")(toLiteral("\"hi there\"")) }
  test("literalList") { assertResult("[1 2 3]")(dumpObject(toLiteralList("[1 2 3]"))) }
  test("literalList2") { assertResult("[1 [2] 3]")(dumpObject(toLiteralList("[1 [2] 3]"))) }
  test("literalList3") { assertResult("[[1 2 3]]")(dumpObject(toLiteralList("[[1 2 3]]"))) }
  test("literalList4") { assertResult("[1 hi true]")(dumpObject(toLiteralList("[1 \"hi\" true]"))) }
  test("literalList5") { assertResult("[[1.0, hi, true]]")(toLiteral("([([1 \"hi\" true])])").toString) }
  test("parseLiteralList") { assertResult("[1 2 3]")(dumpObject(toLiteralList("[1 2 3]"))) }
  test("parseLiteralList2a") { assertResult("[1 [2] 3]")(dumpObject(toLiteralList("[1 [2] 3]"))) }
  test("parseLiteralList2b") { assertResult("[[1] [2] [3]]")(dumpObject(toLiteralList("[[1] [2] [3]]"))) }
  test("parseLiteralList3") { assertResult("[[1 2 3]]")(dumpObject(toLiteralList("[[1 2 3]]"))) }
  test("parseLiteralList4") { assertResult("[1 hi true]")(dumpObject(toLiteralList("[1 \"hi\" true]"))) }

  test("agent and agentset literals surrounded by brackets") {
    val result = toLiteral("{agent-parseable}")
    assertResult("PARSEDAGENTLITERAL")(result)
  }

  test("agent and agentset literals surrounded by brackets when not importing") {
    testError("{all-turtles}", "Can only have literal agents and agentsets if importing.", isImport = false)
  }

  test("badLiteral") { testError("foobar", "Expected a literal value.") }
  test("badLiteralReporter") { testError("round", "Expected a literal value.") }

  test("extension literal") {
    val input = "{{foo: bar baz}}"
    val result = toLiteral(input)
    assertResult("PARSEDEXTENSIONOBJECT")(result)
  }
}
