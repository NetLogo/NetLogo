// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{ CompilationOperand, DummyCompilationEnvironment,
  DummyExtensionManager, ErrorSource, Femto, NetLogoCore, Program, TokenizerInterface }

import AstPath._

import WhiteSpace.Context

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
    val tracker = new WhiteSpace.Tracker(sources)
    procDefs.foldLeft(Context(lastPosition = Some(("", AstPath(), 0)))) {
      case (ctx, procDef) =>
        tracker.visitProcedureDefinition(procDef)(ctx)
    }
  }

  test("basic whitespace") {
    val result = trackWhiteSpace("to foo end")
    assert(result.astWsMap(AstPath(Proc("FOO"))).leading == "to foo")
    assert(result.astWsMap(AstPath(Proc("FOO"))).trailing == "end")
    assert(result.astWsMap(AstPath(Proc("FOO"))).backMargin == " ")
  }

  test("two-procedure whitespace") {
    val result = trackWhiteSpace("to foo end to bar end")
    assert(result.astWsMap(AstPath(Proc("FOO"))).leading == "to foo")
    assert(result.astWsMap(AstPath(Proc("FOO"))).backMargin == " ")
    assert(result.astWsMap(AstPath(Proc("FOO"))).trailing == "end")
    assert(result.astWsMap(AstPath(Proc("BAR"))).leading == " to bar")
    assert(result.astWsMap(AstPath(Proc("BAR"))).backMargin == " ")
    assert(result.astWsMap(AstPath(Proc("BAR"))).trailing == "end")
  }

  test("parenthesized WhiteSpace") {
    val result = trackWhiteSpace("to z show (word \"a\" \"1\") end")
    assert(result.astWsMap(AstPath(Proc("Z"), Stmt(0), RepArg(0))).leading == " (")
    assert(result.astWsMap(AstPath(Proc("Z"), Stmt(0), RepArg(0), RepArg(1))).trailing == ") ")
  }

  test("parenthesized WhiteSpace in lone command tasks") {
    val result = trackWhiteSpace("to z run [ show (word \"a\" \"1\") ] end")
    val proc = AstPath(Proc("Z"))
    val word = proc / Stmt(0) / RepArg(0) / CmdBlk(0) / Stmt(0) / RepArg(0)
    assert(result.astWsMap(word).leading == " (")
    assert(result.astWsMap(word / RepArg(1)).trailing == ") ")
    assert(result.astWsMap(proc).backMargin == " ")
  }

  test("parenthesized WhiteSpace in blocks") {
    val result = trackWhiteSpace("to z run [ show (word \"a\" \"1\") ] fd 1 end")
    val lastArg = AstPath(Proc("Z"), Stmt(0), RepArg(0), CmdBlk(0), Stmt(0), RepArg(0), RepArg(1))
    val word = AstPath(Proc("Z"), Stmt(0), RepArg(0), CmdBlk(0), Stmt(0), RepArg(0))
    val blk = AstPath(Proc("Z"), Stmt(0), RepArg(0), CmdBlk(0))
    assert(result.astWsMap(word).leading == " (")
    assert(result.astWsMap(lastArg).trailing == ") ")
    assert(result.astWsMap(blk).backMargin == ") ")
  }

  test("parenthesized WhiteSpace in reporter blocks") {
    val result = trackWhiteSpace("to z show [ (word \"a\" \"1\") ] of turtles fd 1 end")
    val blk = AstPath(Proc("Z"), Stmt(0), RepArg(0), RepBlk(0))
    val lastArg = blk / RepArg(1)
    assert(result.astWsMap(blk).leading == " (")
    assert(result.astWsMap(lastArg).trailing == ") ")
    assert(result.astWsMap(blk).backMargin == ") ")
  }

  test("parenthesized WhiteSpace in lone reporter tasks") {
    val result = trackWhiteSpace("to-report z report runresult [ (word \"a\" \"1\") ] end")
    val proc = AstPath(Proc("Z"))
    val word = proc / Stmt(0) / RepArg(0) / RepArg(0) / RepArg(0)
    assert(result.astWsMap(word).leading == " (")
    assert(result.astWsMap(word / RepArg(1)).trailing == ") ")
    assert(result.astWsMap(proc).backMargin == " ")
  }

  test("parenthesized WhiteSpace in reporter tasks with other commands") {
    val result = trackWhiteSpace("to-report z let foo runresult [ (word \"a\" \"1\") ] report foo end")
    val word = AstPath(Proc("Z"), Stmt(0), RepArg(1), RepArg(0), RepArg(0))
    val lastArg = word / RepArg(1)
    assert(result.astWsMap(word).leading == " (")
    assert(result.astWsMap(lastArg).trailing == ") ")
    assert(result.astWsMap(word.`../`).backMargin == ") ")
    assert(result.astWsMap(word.`../`).backMargin == ") ")
  }
}
