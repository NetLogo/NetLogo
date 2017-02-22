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
      compile("to __test [x] " + source + "\nend")
        .statements.stmts.head.toString
    }

    def compile(source: String): ProcedureDefinition = {
      val procdef +: _ = Scaffold(source)
      transformer.visitProcedureDefinition(procdef)
    }
  }

  test("let other turtles forces evaluation of lazy agentset") { new Helper {
    assertResult("_let[_force[_other[_turtles[]]]]")(compileCommands("let foo other turtles"))
  } }

  test("let other other turtles forces evaluation of lazy agentset") { new Helper {
    assertResult("_let[_force[_other[_other[_turtles[]]]]]")(compileCommands("let foo other other turtles"))
  } }

  test("let with turtles forces evaluation") { new Helper {
    assertResult("_let[_force[_with[_turtles[], [_constboolean:true[]]]]]")(compileCommands("let foo turtles with [ true ]"))
  } }

  test("let with with turtles forces evaluation once") { new Helper {
    assertResult("_let[_force[_with[_with[_turtles[], [_constboolean:true[]]]]]]")(compileCommands("let foo turtles with [ true ] with [ true ]"))
  } }

  test("let count turtles does not force evaluation") { new Helper {
    assertResult("_let[_count[_turtles[]]]")(compileCommands("let foo count turtles"))
  } }

  test("let count other turtles does not force evaluation") { new Helper {
    assertResult("_let[_count[_other[_turtles[]]]]")(compileCommands("let foo count other turtles"))
  } }

  test("let count with turtles does not force evaluation") { new Helper {
    assertResult("_let[_count[_with[_turtles[], [_constboolean:true[]]]]]")(compileCommands("let foo count turtles with [ true ]"))
  } }

  test("list other turtles forces evaluation") { new Helper {
    assertResult("_let[_list[_force[_other[_turtles[]]]]]")(compileCommands("let foo (list other turtles)"))
  } }

  test("list other with turtles forces evaluation once") { new Helper {
    assertResult("_let[_list[_force[_other[_with[_turtles[], [_constboolean:true[]]]]]]]")(compileCommands("let foo (list other turtles with [ true ])"))
  } }

  test("any? turtles does not force evaluation") { new Helper {
    assertResult("_let[_any[_turtles[]]]")(compileCommands("let foo any? turtles"))
  } }

  test("any? other turtles does not force evaluation") { new Helper {
    assertResult("_let[_any[_other[_turtles[]]]]")(compileCommands("let foo any? other turtles"))
  } }

  test("any? other other turtles does not force evaluation") { new Helper {
    assertResult("_let[_any[_other[_other[_turtles[]]]]]")(compileCommands("let foo any? other other turtles"))
  } }

  test("any? other with turtles does not force evaluation") { new Helper {
    assertResult("_let[_any[_other[_with[_turtles[], [_constboolean:true[]]]]]]")(compileCommands("let foo any? other turtles with [ true ]"))
  } }

//  test("member other does not force evaluation") { new Helper {
//    assertResult()
//  } }
}
