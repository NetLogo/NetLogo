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

  test("let count other turtles does not forces evaluation") { new Helper {
    assertResult("_let[_count[_other[_turtles[]]]]")(compileCommands("let foo count other turtles"))
  } }

  test("let with turtles forces evaluation") { new Helper {
    assertResult("_let[_force[_with[_turtles[], _constboolean:true[]]]]")(compileCommands("let foo turtles with [ true ]"))
  } }

  test("list other turtles forces evalation") { new Helper {
    assertResult("_let[_list[_force[_other[_turtles[]]]]]")(compileCommands("let foo (list other turtles)"))
  } }
}
