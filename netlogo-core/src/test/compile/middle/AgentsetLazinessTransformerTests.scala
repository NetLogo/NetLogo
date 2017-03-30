// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package middle

import org.scalatest.FunSuite
import org.nlogo.prim._
import org.nlogo.compile.api.ProcedureDefinition

class AgentsetLazinessTransformerTests extends FunSuite {
  trait Helper {
    val transformer = new AgentsetLazinessTransformer()

    def compileReporter(source: String) = {
      compile("to-report __test [x] report " + source + "\nend")
        .statements.stmts.head.args.head.toString
    }

    def compileCommands(source: String) = {
      compile("globals [x] to __test " + source + "\nend")
        .statements.stmts.head.toString
    }

    def compile(source: String): ProcedureDefinition = {
      val procdef +: _ = Scaffold(source)
      transformer.visitProcedureDefinition(procdef)
    }
  }

  test("other forces evaluation") { new Helper {
    assertResult("_let[_force[_other[_turtles[]]]]")(compileCommands("let foo other turtles"))
    assertResult("_let[_force[_other[_other[_turtles[]]]]]")(compileCommands("let foo other other turtles"))
  } }

  test("with forces evaluation") { new Helper {
    assertResult("_let[_force[_with[_turtles[], [_constboolean:true[]]]]]")(compileCommands("let foo turtles with [ true ]"))
    assertResult("_let[_force[_with[_with[_turtles[], [_constboolean:true[]]], [_constboolean:true[]]]]]")(compileCommands("let foo turtles with [ true ] with [ true ]"))
    assertResult("_set[_observervariable:0[], _force[_with[_turtles[], [_constboolean:true[]]]]]")(compileCommands("set x turtles with [ true ]"))
  } }

  test("other with forces evaluation") { new Helper {
    assertResult("_let[_force[_other[_with[_turtles[], [_constboolean:true[]]]]]]")(compileCommands("let foo other turtles with [ true ]"))
  } }

  test("count does not force evaluation") { new Helper {
    assertResult("_let[_count[_turtles[]]]")(compileCommands("let foo count turtles"))
    assertResult("_let[_count[_other[_turtles[]]]]")(compileCommands("let foo count other turtles"))
    assertResult("_let[_count[_with[_turtles[], [_constboolean:true[]]]]]")(compileCommands("let foo count turtles with [ true ]"))
  } }

  test("any? does not force evaluation") { new Helper {
    assertResult("_let[_any[_turtles[]]]")(compileCommands("let foo any? turtles"))
    assertResult("_let[_any[_other[_turtles[]]]]")(compileCommands("let foo any? other turtles"))
    assertResult("_let[_any[_other[_other[_turtles[]]]]]")(compileCommands("let foo any? other other turtles"))
    assertResult("_let[_any[_other[_with[_turtles[], [_constboolean:true[]]]]]]")(compileCommands("let foo any? other turtles with [ true ]"))
  } }

  test("n-of does not force evaluation") { new Helper {
    assertResult("_let[_nof[_constdouble:3.0[], _turtles[]]]")(compileCommands("let foo n-of 3 turtles"))
    assertResult("_let[_nof[_constdouble:3.0[], _other[_turtles[]]]]")(compileCommands("let foo n-of 3 other turtles"))
    assertResult("_let[_nof[_constdouble:3.0[], _other[_with[_turtles[], [_constboolean:true[]]]]]]")(compileCommands("let foo n-of 3 other turtles with [ true ]"))
  } }

  test("one-of does not force evaluation") { new Helper {
    assertResult("_let[_oneof[_turtles[]]]")(compileCommands("let foo one-of turtles"))
    assertResult("_let[_oneof[_other[_turtles[]]]]")(compileCommands("let foo one-of other turtles"))
    assertResult("_let[_oneof[_other[_with[_turtles[], [_constboolean:true[]]]]]]")(compileCommands("let foo one-of other turtles with [ true ]"))
  } }

  test("member does not force evaluation") { new Helper {
    assertResult("_let[_member[_turtle[_constdouble:0.0[]], _with[_turtles[], [_constboolean:true[]]]]]")(compileCommands("let foo member? turtle 0 turtles with [ true ]"))
    assertResult("_let[_member[_turtle[_constdouble:0.0[]], _other[_turtles[]]]]")(compileCommands("let foo member? turtle 0 other turtles"))
  } }

  test("list forces evaluation") { new Helper {
    assertResult("_let[_list[_force[_other[_turtles[]]]]]")(compileCommands("let foo (list other turtles)"))
    assertResult("_let[_list[_force[_other[_with[_turtles[], [_constboolean:true[]]]]]]]")(compileCommands("let foo (list other turtles with [ true ])"))
  } }


}
