// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.scalatest.FunSuite

import org.nlogo.core.{ CompilerException, SourceLocation, Token, TokenType }
import org.nlogo.core.prim.{ _procedurevariable, _lambdavariable }
import org.nlogo.core.TokenDSL._

class ArrowLambdaScoperTests extends FunSuite {
  val testSymbols = SymbolTable(
    "ASK"  -> SymbolType.PrimitiveCommand,
    "MEAN" -> SymbolType.PrimitiveReporter,
    "BAR"  -> SymbolType.GlobalVariable)

  def scope(ts: Seq[Token]): Option[(Seq[String], Seq[Token], SymbolTable)] = ArrowLambdaScoper(ts, testSymbols)

  def testScopes(toks: Seq[Token], expectedArgs: Seq[String], expectedBody: Seq[Token], expectedSymbols: SymbolTable = SymbolTable.empty) = {
    val res = scope(toks)
    assert(res.isDefined)
    res.foreach {
      case (args, body, symbols) =>
        assertResult(expectedArgs)(args)
        assertResult(expectedBody)(body)
        expectedSymbols.foreach {
          case (key, symType) =>
            assert(symbols.contains(key))
            assertResult(symType)(symbols(key))
        }
    }
  }

  test("non-blocks return None") {
    assert(scope(Seq()).isEmpty)
    assert(scope(Seq(`[`)).isEmpty)
    assert(scope(Seq(`[`, id("foo"))).isEmpty)
    assert(scope(Seq(`[`, `[`, id("foo"))).isEmpty)
    assert(scope(Seq(`]`)).isEmpty)
  }

  test("blocks with no arrow return None") {
    assert(scope(Seq(`[`, `]`)).isEmpty)
    assert(scope(Seq(`[`, `[`, `]`, `]`)).isEmpty)
  }

  test("a block with a nested list of IDs returns none") {
    assert(scope(Seq(`[`, id("baz"), `[`, `[`, id("qux"), `]`, `->`, id("qux"), `]`, `]`)).isEmpty)
  }

  /*
  //TODO: This case under discussion
  test("a block with only an arrow has empty arguments and body") {
    testScopes(Seq(`[`, `->`, `]`), Seq(), Seq())
  }
  */

  test("a block with arguments returns its arguments") {
    testScopes(Seq(`[`, `[`, unid("foo"), unid("baz"),`]`, `->`, `]`),
      Seq("FOO", "BAZ"), Seq(),
      SymbolTable("FOO" -> SymbolType.LocalVariable, "BAZ" -> SymbolType.LocalVariable))
  }

  test("a block binds the bound id in each occurence in the body") {
    testScopes(Seq(`[`, `[`, unid("foo"), `]`, `->`, unid("foo"), `]`),
      Seq("FOO"), Seq(Token("foo", TokenType.Reporter, _lambdavariable("FOO"))(SourceLocation(0, 0, "test"))),
      SymbolTable("FOO" -> SymbolType.LocalVariable))
  }

  test("a block with body returns its body") {
    testScopes(Seq(`[`, `[`, `]`, `->`, lit(2), `]`), Seq(), Seq(lit(2)))
  }

  test("a block with an arrow in the argument list errors") {
    intercept[CompilerException] { scope(Seq(`[`, `[`, `->`, `]`, `]`)) }
  }

  test("a block with a bound id in the argument list errors") {
    val boundToken = Token("foo", TokenType.Reporter, new _procedurevariable(0, "foo"))(SourceLocation(0, 0, "test"))
    intercept[CompilerException] {
      scope(Seq(`[`, `[`, boundToken, `]`, `->`, `]`))
    }
  }

  def testArgError(args: Seq[Token], expectedError: String): Unit = {
    val ex = intercept[CompilerException] { scope(Seq(`[`, `[`) ++ args ++ Seq(`]`, `->`, `]`)) }
    assert(ex.getMessage.contains(expectedError))
  }

  test("a block with a literal in the argument list errors") {
    testArgError(Seq(lit(2)), "Expected a variable name here")
  }

  test("errors when lambda variable shadows enclosing lambda variable") {
    testArgError(Seq(lamvar("bar")), "There is already a global variable called BAR")
  }
}
