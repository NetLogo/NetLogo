// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile

import org.scalatest.FunSuite
import org.nlogo.api.{ CompilerException, DummyExtensionManager, Program }
import org.nlogo.{ nvm, parse }

class AgentTypeCheckerTests extends FunSuite {

  /// first some helpers
  def compile(source: String, is3D: Boolean): Seq[parse.ProcedureDefinition] = {
    import parse._
    val tokenizer = if (is3D) parse.Parser.Tokenizer3D else parse.Parser.Tokenizer2D
    val results = new parse.StructureParser(
      tokenizer.tokenize(source), None, parse.StructureParser.emptyResults(is3D))
      .parse(false)
    val defs = new collection.mutable.ArrayBuffer[parse.ProcedureDefinition]
    for (procedure <- results.procedures.values) {
      new LetScoper(procedure, results.tokens(procedure), results.program.usedNames).scan()
      val tokens =
        new IdentifierParser(results.program, nvm.CompilerInterface.NoProcedures,
                             results.procedures, new DummyExtensionManager)
          .process(results.tokens(procedure).iterator, procedure)
      defs ++= new ExpressionParser(procedure).parse(tokens)
    }
    new AgentTypeChecker(defs).check()
    defs
  }
  def testBoth(source: String, expected: String) {
    testOne(source, expected, false)
    testOne(source, expected, true)
  }
  def testOne(source: String, expected: String, is3D: Boolean) {
    val defs = compile(source, is3D)
    val buf = new StringBuilder
    assertResult(expected)(
      defs.map { pd: parse.ProcedureDefinition =>
          pd.procedure.name + ":" + pd.procedure.usableBy }
        .mkString(" "))
  }
  def testError(source: String, error: String) {
    doTestError(source, error, false)
    doTestError(source, error, true)
  }
  def doTestError(source: String, error: String, is3D: Boolean) {
    val e = intercept[CompilerException] {
      compile(source, is3D)
    }
    assertResult(error)(e.getMessage)
  }
  /// tests not involving blocks (easy)
  test("easy1") { testBoth("to foo end", "FOO:OTPL") }
  test("easy2") { testBoth("to foo fd 1 end", "FOO:-T--") }
  test("easy3") { testBoth("to foo sprout 1 end", "FOO:--P-") }
  test("easy4") { testBoth("to foo print link-length end", "FOO:---L") }
  test("easy5") { testBoth("to foo crt 1 end", "FOO:O---") }
  test("easy6") { testBoth("to foo set pcolor red end", "FOO:-TP-") }
  test("oneProcedure1") {
    testError("to foo fd 1 sprout 1 end",
      "You can't use sprout in a turtle context, because sprout is patch-only.")
  }
  test("oneProcedure2") {
    testError("to foo set pcolor red crt 1 end",
      "You can't use crt in a turtle/patch context, because crt is observer-only.")
  }
  test("oneProcedure3") {
    testError("to foo set color red crt 1 end",
      "You can't use crt in a turtle/link context, because crt is observer-only.")
  }
  test("oneProcedure4") {
    testError("to foo crt 1 set color red end",
      "You can't use COLOR in an observer context, because COLOR is turtle/link-only.")
  }
  test("twoProcedures1") {
    testError("to foo fd 1 bar end  to bar sprout 1 end",
      "You can't use BAR in a turtle context, because BAR is patch-only.")
  }
  test("twoProcedures2") {
    testError("to foo bar fd 1 end  to bar sprout 1 end",
      "You can't use fd in a patch context, because fd is turtle-only.")
  }
  test("chained1") {
    testError("to foo1 fd 1 foo2 end  to foo2 foo3 end  to foo3 foo4 end  to foo4 foo5 end  to foo5 sprout 1 end",
      "You can't use FOO2 in a turtle context, because FOO2 is patch-only.")
  }
  test("chained2") {
    testError("to foo1 fd 1 end  to foo2 foo1 end  to foo3 foo2 end  to foo4 foo3 end  to foo5 sprout 1 foo4 end",
      "You can't use FOO4 in a patch context, because FOO4 is turtle-only.")
  }
  test("chained3") {
    testError("to foo2 foo1 end  to foo1 fd 1 end  to foo4 foo3 end  to foo3 foo2 end  to foo5 sprout 1 foo4 end",
      "You can't use FOO4 in a patch context, because FOO4 is turtle-only.")
  }

  /// tests involving blocks (harder!)

  test("ifelse") {
    testError("to foo1 ifelse true [ fd 1 ] [ sprout 1 ] end",
      "You can't use sprout in a turtle context, because sprout is patch-only.")
  }
  test("sprout") {
    testError("to foo sprout 1 [ print link-length ] end",
      "You can't use link-length in a turtle context, because link-length is link-only.")
  }
  test("ask1") {
    testError("to foo [x] ask x [ crt 1 ] end",
      "You can't use crt in a turtle/patch/link context, because crt is observer-only.")
  }
  test("ask2") {
    testError("to foo ask turtles [ crt 1 ] end",
      "You can't use crt in a turtle context, because crt is observer-only.")
  }
  test("ask3") {
    testError("to foo ask patches [ set color red ] end",
      "You can't use COLOR in a patch context, because COLOR is turtle/link-only.")
  }
  test("askWith") {
    testError("to foo ask turtles with [color = red] [ crt 1 ] end",
      "You can't use crt in a turtle context, because crt is observer-only.")
  }
  test("crt1") {
    testBoth("to foo crt 1 [ print who ] end", "FOO:O---") }
  test("crt2") {
    testError("to foo crt 1 [ sprout 1 ] end", "You can't use sprout in a turtle context, because sprout is patch-only.") }
  test("crt3") {
    testError("to foo crt 1 [ crt 1 ] end", "You can't use crt in a turtle context, because crt is observer-only.") }
}
