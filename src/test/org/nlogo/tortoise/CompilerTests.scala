// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise

import org.scalatest.FunSuite
import org.nlogo.api

// Mostly we test the compiler by running the results. But at least, occasionally we're interested
// in *exactly* what the JavaScript output looks like.

class CompilerTests extends FunSuite {

  /// compileReporter

  test("literals") {
    import Compiler.{compileReporter => compile}
    assertResult("1")(
      compile("1"))
    assertResult("1")(
      compile("1.0"))
    assertResult("[]")(
      compile("[]"))
    assertResult("[1, [2], 3]")(
      compile("[1 [2] 3]"))
  }

  test("arithmetic expressions") {
    import Compiler.{compileReporter => compile}
    assertResult("(2 + 2)")(
      compile("2 + 2"))
    assertResult("((1 + 2) * 3)")(
      compile("(1 + 2) * 3"))
    assertResult("(1 + (2 * 3))")(
      compile("1 + 2 * 3"))
    assertResult("((1 + 2) + (3 + 4))")(
      compile("(1 + 2) + (3 + 4)"))
  }

  test("equality"){
    import Compiler.{compileReporter => compile}
    assertResult("(2 === 2)")(compile("2 = 2"))
    assertResult("""("hello" === "hello")""")(compile(""""hello" = "hello""""))
  }

  test("reporters: word") {
    import Compiler.{compileReporter => compile}
    val input = "(word 1 2 3)"
    val expected = """(Dump("") + Dump(1) + Dump(2) + Dump(3))"""
    assertResult(expected)(compile(input))
  }

  // compileCommands

  test("commands: let") {
    import Compiler.{compileCommands => compile}
    val input = "let x 5 output-print x"
    val expected = """|var X = 5;
                      |Prims.outputprint(X)""".stripMargin
    assertResult(expected)(compile(input))
  }

  test("commands: ask simple") {
    import Compiler.{compileCommands => compile}
    val input = "ask turtles [fd 1]"
    val expected = "AgentSet.ask(world.turtles(), true, function(){ Prims.fd(1) });"
    assertResult(expected)(compile(input))
  }

  test("commands: ask patches with variable") {
    import Compiler.{compileCommands => compile}
    val input = "ask patches [output-print pxcor]"
    val expected = "AgentSet.ask(world.patches(), true, function(){ Prims.outputprint(AgentSet.getPatchVariable(0)) });"
    assertResult(expected)(compile(input))
  }

  test("commands: with") {
    import Compiler.{compileCommands => compile}
    val input = "ask patches with [pxcor = 1] [output-print pycor]"
    val expectedAgentFilter =
      "AgentSet.agentFilter(world.patches(), function(){ return (AgentSet.getPatchVariable(0) === 1) })"
    val expected = s"AgentSet.ask($expectedAgentFilter, true, function(){ Prims.outputprint(AgentSet.getPatchVariable(1)) });"
    assertResult(expected)(compile(input))
  }

  /// compileProcedures

  test("command procedure") {
    import Compiler.{compileProcedures => compile}
    val input = "to foo output-print 5 end"
    val expected = """world = new World(0, 0, 0, 0, 12.0, true, true, {}, {}, 0);
                     |function FOO () {
                     |Prims.outputprint(5)
                     |};
                     |""".stripMargin
    assertResult(expected)(compile(input)._1)
  }

  test("globals: accessed by number") {
    import Compiler.{compileProcedures => compile}
    val input = "globals [x y z] to foo-bar? output-print z output-print y output-print x end"
    val expected =
     """|Globals.init(3)
        |world = new World(0, 0, 0, 0, 12.0, true, true, {}, {}, 0);
        |function FOO_BAR_P () {
        |Prims.outputprint(Globals.getGlobal(2))
        |Prims.outputprint(Globals.getGlobal(1))
        |Prims.outputprint(Globals.getGlobal(0))
        |};
        |""".stripMargin
    assertResult(expected)(compile(input)._1)
  }

}
