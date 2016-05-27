// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{CommandBlock, CompilerException, ProcedureDefinition, ReporterApp}
import org.scalatest.FunSuite

class AgentTypeCheckerTests extends FunSuite {

  /// first some helpers
  def compile(source: String): Seq[ProcedureDefinition] = {
    val (coreDefs, _) = FrontEnd.frontEnd(source)
    coreDefs
  }

  def testOne(source: String, expected: String) {
    val defs = compile(source)
    assertResult(expected)(
      defs.map { pd: ProcedureDefinition =>
          pd.procedure.name + ":" + pd.procedure.agentClassString }
        .mkString(" "))
  }
  def testError(source: String, error: String) {
    val e = intercept[CompilerException] {
      compile(source)
    }
    assertResult(error)(e.getMessage)
  }

  /// tests not involving blocks (easy)
  test("easy1") { testOne("to foo end", "FOO:OTPL") }
  test("easy2") { testOne("to foo fd 1 end", "FOO:-T--") }
  test("easy3") { testOne("to foo sprout 1 end", "FOO:--P-") }
  test("easy4") { testOne("to foo print link-length end", "FOO:---L") }
  test("easy5") { testOne("to foo crt 1 end", "FOO:O---") }
  test("easy6") { testOne("to foo set pcolor red end", "FOO:-TP-") }
  test("recursive1") { testOne("to foo set pcolor red foo end", "FOO:-TP-") }
  test("recursive2") { testOne("to foo set pcolor red foo2 end to foo2 set color blue foo end", "FOO:-T-- FOO2:-T--") }
  test("oneProcedure1") {
    testError("to foo fd 1 sprout 1 end",
      "You can't use SPROUT in a turtle context, because SPROUT is patch-only.")
  }
  test("oneProcedure2") {
    testError("to foo set pcolor red crt 1 end",
      "You can't use CRT in a turtle/patch context, because CRT is observer-only.")
  }
  test("oneProcedure3") {
    testError("to foo set color red crt 1 end",
      "You can't use CRT in a turtle/link context, because CRT is observer-only.")
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
      "You can't use FD in a patch context, because FD is turtle-only.")
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
      "You can't use SPROUT in a turtle context, because SPROUT is patch-only.")
  }
  test("sprout") {
    testError("to foo sprout 1 [ print link-length ] end",
      "You can't use LINK-LENGTH in a turtle context, because LINK-LENGTH is link-only.")
  }
  test("ask1") {
    testError("to foo [x] ask x [ crt 1 ] end",
      "You can't use CRT in a turtle/patch/link context, because CRT is observer-only.")
  }
  test("ask2") {
    testError("to foo ask turtles [ crt 1 ] end",
      "You can't use CRT in a turtle context, because CRT is observer-only.")
  }
  test("ask3") {
    testError("to foo ask patches [ set color red ] end",
      "You can't use COLOR in a patch context, because COLOR is turtle/link-only.")
  }
  test("askWith") {
    testError("to foo ask turtles with [color = red] [ crt 1 ] end",
      "You can't use CRT in a turtle context, because CRT is observer-only.")
  }
  test("crt1") {
    testOne("to foo crt 1 [ print who ] end", "FOO:O---") }
  test("crt2") {
    testError("to foo crt 1 [ sprout 1 ] end",
      "You can't use SPROUT in a turtle context, because SPROUT is patch-only.") }
  test("crt3") {
    testError("to foo crt 1 [ crt 1 ] end",
      "You can't use CRT in a turtle context, because CRT is observer-only.") }

  // the next tests check correctness of task type-checking
  test("map") {
    val foo = compile("to foo print map [ my-links ] [ 1 2 3 ] end").head
    val rt = foo.statements.stmts.head
      .args.head.asInstanceOf[ReporterApp]
      .args.head.asInstanceOf[ReporterApp]
    assertResult("OTPL")(rt.reporter.agentClassString)
    assertResult(Some("-T--"))(rt.reporter.blockAgentClassString)
  }

  test("tasks type-check properly") {
    val foo = compile("to foo ask turtles [ let bar task [ print 1 ] ] end").head
    assertResult(foo.procedure.agentClassString)("OTPL")
    val barTask =
      foo.statements.stmts.head.args(1).asInstanceOf[CommandBlock]
         .statements.stmts.head.args(0).asInstanceOf[ReporterApp]
    assertResult("OTPL")(barTask.reporter.agentClassString)
  }
}
