// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile

import org.nlogo.compile.api.{ MiddleEndInterface, Optimizations, ProcedureDefinition }
import org.nlogo.core.{ DummyCompilationEnvironment, Femto, Program }
import org.nlogo.util.AnyFunSuiteEx

class MiddleEndTest extends AnyFunSuiteEx {
  val middleEnd =
    Femto.scalaSingleton[MiddleEndInterface]("org.nlogo.compile.middle.MiddleEnd")

  def compile(keyword: String, source: String): ProcedureDefinition = {
    val program = Program.empty()
    val procdefs = TestHelper.compiledProcedures(keyword + " foo " + source + "\nend", program)
    assertResult(1)(procdefs.size)
    val alteredProcDefs = middleEnd.middleEnd(
      procdefs,
      program,
      Map("" -> source),
      new DummyCompilationEnvironment,
      Optimizations.none)
    alteredProcDefs.head
  }

  def format(procedure: ProcedureDefinition): String = {
    procedure.statements.stmts.mkString("\n")
  }

  test("repeat without lambda does not introduce scope") {
    assertResult("_repeatlocal:1,+0[_constdouble:5.0[], [_setprocedurevariable:A[_constdouble:5.0[]] _print[_procedurevariable:A[]]]]")(
      format(compile("to", "repeat 5 [ let a 5 print a ]")))
  }

  test("repeat with lambda scopes its block") {
    assertResult("_repeatlocal:0,+0[_constdouble:5.0[], [_enterscope[] _let[_constdouble:5.0[]] _run[_commandlambda[]] _exitscope[]]]")(
      format(compile("to", "repeat 5 [ let a 5 run [ -> print a ] ]")))
  }

  test("repeat with ask inside lambda scopes its block") {
    assertResult("_repeatlocal:0,+0[_constdouble:5.0[], [_enterscope[] _let[_constdouble:5.0[]] _ask:+0[_turtles[], [_run[_commandlambda[]]]] _exitscope[]]]")(
      format(compile("to", "repeat 5 [ let a 5 ask turtles [ run [ -> print a ] ] ]")))
  }

  test("repeat with if scopes its block") {
    assertResult("_repeatlocal:0,+0[_constdouble:5.0[], [_enterscope[] _if:+0[_constboolean:true[], [_let[_constdouble:5.0[]] _run[_commandlambda[]]]] _exitscope[]]]")(
      format(compile("to", "repeat 5 [ if true [ let a 5 run [ -> print a ] ] ]")))
  }

  test("repeat with ask introducing a let does not scope its block") {
    assertResult("_repeatlocal:0,+0[_constdouble:5.0[], [_ask:+0[_turtles[], [_let[_constdouble:5.0[]] _run[_commandlambda[]]]]]]")(
      format(compile("to", "repeat 5 [ ask turtles [ let a 5 run [ -> print a ] ] ]")))
  }
}

