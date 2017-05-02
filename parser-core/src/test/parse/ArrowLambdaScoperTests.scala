// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.scalatest.FunSuite

import org.nlogo.core.{ CompilerException, SourceLocation, Token, TokenType }
import org.nlogo.core.prim.{ _lambdavariable, Lambda }, Lambda.Arguments
import org.nlogo.core.TokenDSL._
import PrimDSL._

class ArrowLambdaScoperTests extends FunSuite {
  test("blocks with no arrow return None") {
    assert(scope(Seq(`[`, `]`)).isEmpty)
    assert(scope(Seq(`[`, `[`, `]`, `]`)).isEmpty)
  }

  test("a block with a nested list of IDs returns none") {
    assert(scope(Seq(`[`, id("baz"), `[`, `[`, id("qux"), `]`, `->`, id("qux"), `]`, `]`)).isEmpty)
  }

  test("an unbracketed non-procedure block with a bound first symbol returns None") {
    val tokens = Seq(`[`, id("foreach"), `[`, lit(1), lit(2), `]`, `[`, `->`, `]`, `]`)
    val symTable = SymbolTable("FOREACH" -> SymbolType.PrimitiveCommand)
    assert(scope(tokens, symTable) == None)
  }

  test("an extension prim as the first symbol in non-procedure block returns None") {
    val tokens = Seq(`[`, ex("rnd:weighted-n-of-list"), lit(3),
      `[`, lit(1), lit(2), `]`,
      `[`, `[`, id("i"), `]`, `->`, id("i"), `]`,
      `]`)
    assert(scope(tokens) == None)
  }

  test("a block with arguments returns its arguments") {
    testScopes(Seq(`[`, `[`, unid("foo"), unid("baz"),`]`, `->`, `]`),
      Seq("FOO", "BAZ"), Seq(),
      SymbolTable("FOO" -> SymbolType.LambdaVariable, "BAZ" -> SymbolType.LambdaVariable))
  }

  test("a block binds the bound id in each occurence in the body") {
    testScopes(Seq(`[`, `[`, unid("foo"), `]`, `->`, unid("foo"), `]`),
      Seq("FOO"), Seq(Atom(Token("foo", TokenType.Reporter, _lambdavariable("FOO"))(SourceLocation(0, 0, "test")))),
      SymbolTable("FOO" -> SymbolType.LambdaVariable))
  }

  test("an unbracketed block binds the bound id in each occurence in the body") {
    testScopes(Seq(`[`, unid("foo"), `->`, unid("foo"), `]`),
      Seq("FOO"), Seq(Atom(Token("foo", TokenType.Reporter, _lambdavariable("FOO"))(SourceLocation(0, 0, "test")))),
      SymbolTable("FOO" -> SymbolType.LambdaVariable))
  }

  test("a block with body returns its body") {
    testScopes(Seq(`[`, `[`, `]`, `->`, lit(2), `]`), Seq(), Seq(Atom(lit(2))))
  }

  test("a block with no arguments before the arrow returns the body") {
    testScopes(Seq(`[`, `->`, `]`), Seq(), Seq())
  }

  test("a block with an arrow in the argument list returns that it is not a lambda block") {
    assert(scope(Seq(`[`, `[`, `->`, `]`, `]`)).isEmpty)
  }

  test("a block with more than two groups before arrow errors") {
    intercept[CompilerException] { scope(Seq(`[`, `[`, `]`, unid("abc"), `->`, `]`)) }
  }

  test("a block with a literal in the argument list errors") {
    testArgError(Seq(lit(2)), "Expected a variable name here")
  }

  test("errors when lambda variable shadows existing local variable") {
    testArgError(Seq(unid("qux")), "There is already a local variable here called QUX", "QUX" -> SymbolType.ProcedureVariable)
  }

  def testArgError(args: Seq[Token], expectedError: String, bindings: (String, SymbolType)*): Unit = {
    val symTable = SymbolTable(bindings: _*)
    val ex = intercept[CompilerException] { scope(Seq(`[`, `[`) ++ args ++ Seq(`]`, `->`, `]`), symTable) }
    assert(ex.getMessage.contains(expectedError))
  }

  val testSymbols = SymbolTable(
    "ASK"  -> SymbolType.PrimitiveCommand,
    "MEAN" -> SymbolType.PrimitiveReporter,
    "BAR"  -> SymbolType.GlobalVariable)

  def scope(ts: Seq[Token], otherSymbols: SymbolTable = SymbolTable.empty): Option[(Arguments, Seq[SyntaxGroup], SymbolTable)] = {
    val groups = ExpressionParser.groupSyntax(ts.iterator.buffered).get.head.asInstanceOf[BracketGroup]
    ArrowLambdaScoper(groups, testSymbols ++ otherSymbols)
  }

  def testScopes(toks: Seq[Token], expectedArgs: Seq[String], expectedBody: Seq[SyntaxGroup], expectedSymbols: SymbolTable = SymbolTable.empty) = {
    val res = scope(toks)
    assert(res.isDefined)
    res.foreach {
      case (args, body, symbols) =>
        assertResult(expectedArgs)(args.argumentNames)
        assertResult(expectedBody)(body)
        expectedSymbols.foreach {
          case (key, symType) =>
            assert(symbols.contains(key))
            assertResult(symType)(symbols(key))
        }
    }
  }
}
