// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{ CompilationOperand, DummyCompilationEnvironment,
  DummyExtensionManager, ErrorSource, Femto, NetLogoCore, Program, TokenizerInterface }

import AstPath._

import WhiteSpace.Context
import WhiteSpace._

import org.scalatest.FunSuite

class WhiteSpaceTests extends FunSuite with NetLogoParser {
  val tokenizer: TokenizerInterface =
    Femto.scalaSingleton[TokenizerInterface]("org.nlogo.lex.Tokenizer")

  def trackWhiteSpace(source: String): WhiteSpace.Context = {
    val sources = Map("" -> source)
    val compilationOperand = new CompilationOperand(
      sources,
      new DummyExtensionManager(),
      new DummyCompilationEnvironment(),
      Program.fromDialect(NetLogoCore),
      subprogram = false)
    val (procDefs, _) = basicParse(compilationOperand)
    val tracker = new WhiteSpace.Tracker(sources, tokenizer)
    procDefs.foldLeft(Context.empty("")) {
      case (ctx, procDef) =>
        tracker.visitProcedureDefinition(procDef)(ctx)
    }
  }

  val foo = AstPath(Proc("FOO"))
  val z = AstPath(Proc("Z"))

  test("basic whitespace") {
    val result = trackWhiteSpace("to foo end")
    assert(result.whitespaceMap(foo -> Leading) == "to foo")
    assert(result.whitespaceMap(foo -> Trailing) == "end")
  }

  test("procedure arguments") {
    val result = trackWhiteSpace("to foo [bar baz]\nend")
    assert(result.whitespaceMap(foo -> Leading) == "to foo [bar baz]")
  }

  test("whitespace with comments") {
    val result = trackWhiteSpace("to foo\n;comment [here]\nend")
    assert(result.whitespaceMap(foo -> Leading) == "to foo")
    assert(result.whitespaceMap(foo -> Trailing) == "end")
    assert(result.whitespaceMap(foo -> BackMargin) == "\n;comment [here]\n")
  }

  test("infix whitespace") {
    val result = trackWhiteSpace("to foo __ignore 1 + 2 end")
    val two = foo / Stmt(0) / RepArg(0) / RepArg(1)
    assert(result.whitespaceLog.leading(two) == " ")
  }

  test("two-procedure whitespace") {
    val result = trackWhiteSpace("to foo end to bar end")
    val bar = AstPath(Proc("BAR"))
    assert(result.whitespaceLog.leading(foo) == "to foo")
    assert(result.whitespaceLog.trailing(foo) == "end")
    assert(result.whitespaceLog.leading(bar) == " to bar")
    assert(result.whitespaceLog.trailing(bar) == "end")
  }

  test("white space in blocks") {
    val result = trackWhiteSpace("to z __ignore [pycor] of one-of patches end")
    val pycor = z / Stmt(0) / RepArg(0) / RepBlk(0) / RepArg(0)
    assert(result.whitespaceLog.leading(pycor) == "")
  }

  test("parenthesized WhiteSpace in blocks") {
    val result = trackWhiteSpace("to z run [ show (word \"a\" \"1\") ] fd 1 end")
    val lastArg = AstPath(Proc("Z"), Stmt(0), RepArg(0), CmdBlk(0), Stmt(0), RepArg(0), RepArg(1))
    val word = AstPath(Proc("Z"), Stmt(0), RepArg(0), CmdBlk(0), Stmt(0), RepArg(0))
    val blk = AstPath(Proc("Z"), Stmt(0), RepArg(0), CmdBlk(0))
    assert(result.whitespaceLog.leading(word) == " (")
    assert(result.whitespaceLog.trailing(lastArg) == ") ")
    assert(result.whitespaceLog.backMargin(blk) == ") ")
  }

  test("parenthesized WhiteSpace in lone command tasks") {
    val result = trackWhiteSpace("to z run [ show (word \"a\" \"1\") ] end")
    val word = z / Stmt(0) / RepArg(0) / CmdBlk(0) / Stmt(0) / RepArg(0)
    assert(result.whitespaceLog.leading(word) == " (")
    assert(result.whitespaceLog.trailing(word / RepArg(1)) == ") ")
  }

  test("parenthesized WhiteSpace at end of procedure") {
    val result = trackWhiteSpace("to z show (word \"a\" \"1\") end")
    assert(result.whitespaceLog.leading(AstPath(Proc("Z"), Stmt(0), RepArg(0))) == " (")
    assert(result.whitespaceLog.trailing(AstPath(Proc("Z"), Stmt(0), RepArg(0), RepArg(1))) == ") ")
  }

  test("parenthesized WhiteSpace in reporter blocks") {
    val result = trackWhiteSpace("to z show [ (word \"a\" \"1\") ] of turtles fd 1 end")
    val blk = AstPath(Proc("Z"), Stmt(0), RepArg(0), RepBlk(0))
    val word = blk / RepArg(0)
    assert(result.whitespaceMap(word -> Leading) == " (")
    assert(result.whitespaceMap(blk -> BackMargin) == ") ")
    assert(result.whitespaceMap(word / RepArg(1) -> Trailing) == ") ")
  }

  test("parenthesized WhiteSpace in lone reporter tasks") {
    val result = trackWhiteSpace("to-report z report runresult [ (word \"a\" \"1\") ] end")
    val word = z / Stmt(0) / RepArg(0) / RepArg(0) / RepArg(0)
    assert(result.whitespaceLog.leading(word) == " (")
    assert(result.whitespaceLog.trailing(word / RepArg(1)) == ") ")
  }

  test("whitespace for lambdas") {
    val result = trackWhiteSpace("to-report z report [[x y] -> x + y] end")
    val lambda = z / Stmt(0) / RepArg(0)
    val x = z / Stmt(0) / RepArg(0) / RepArg(0) / RepArg(0)
    assert(result.whitespaceMap(x -> Leading) == " ")
    assert(result.whitespaceMap(lambda -> FrontMargin) == "[x y] ->")
  }

  test("parenthesized WhiteSpace in reporter tasks with other commands") {
    val result = trackWhiteSpace("to-report z let foo runresult [ (word \"a\" \"1\") ] report foo end")
    val word = z / Stmt(0) / RepArg(1) / RepArg(0) / RepArg(0)
    val lastArg = word / RepArg(1)
    assert(result.whitespaceMap(word -> Leading) == " (")
    assert(result.whitespaceMap(lastArg -> Trailing) == ") ")
    assert(result.whitespaceMap(word.`../` -> BackMargin) == ") ")
    assert(result.whitespaceMap(word.`../` -> BackMargin) == ") ")
  }
}
