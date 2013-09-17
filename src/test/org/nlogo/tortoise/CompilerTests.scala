// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise

import org.scalatest.FunSuite

class CompilerTests extends FunSuite {

  test("clear-all") {
    import Compiler.{compileCommands => compile}
    assertResult("world.clearall()") {
      compile("clear-all") }
  }

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

  test("list construction") {
    import Compiler.{compileReporter => compile}
    assertResult("Prims.list(1)")(compile("(list 1)"))
    assertResult("Prims.list(1, 2)")(compile("list 1 2"))
    assertResult("Prims.list(world.minPxcor)")(compile("(list min-pxcor)"))
  }

  test("max") {
    import Compiler.{compileReporter => compile}
    assertResult("Prims.max([1, 2, 3])")(compile("max [1 2 3]"))
  }

  test("commands: arithmetic + printing") {
    import Compiler.{compileCommands => compile}
    val expected = """|println((2 + 2))
                      |println((3 * 3))""".stripMargin
    assertResult(expected)(
      compile("output-print 2 + 2 output-print 3 * 3"))
  }

  test("commands: turtle creation") {
    import Compiler.{compileCommands => compile}
    val expected = """|world.createorderedturtles(5);
                      |println(AgentSet.count(world.turtles()))""".stripMargin
    assertResult(expected)(
      compile("cro 5 output-print count turtles"))
  }

  test("commands: while true") {
    import Compiler.{compileCommands => compile}
    val input = "while [true] [output-print 0]"
    val expected =
      """while (true) {
        |println(0)
        |}""".stripMargin
    assertResult(expected)(compile(input))
  }

  test("commands: let") {
    import Compiler.{compileCommands => compile}
    val input = "let x 5 output-print x"
    val expected = """|var X = 5;
                      |println(X)""".stripMargin
    assertResult(expected)(compile(input))
  }

  test("command procedure") {
    import Compiler.{compileProcedures => compile}
    val input = "to foo output-print 5 end"
    val expected = """world = new World(0, 0, 0, 0);
                     |function FOO () {
                     |println(5)
                     |};""".stripMargin
    assertResult(expected)(compile(input)._1)
  }

  test("commands: ask simple") {
    import Compiler.{compileCommands => compile}
    val input = "__ask-sorted turtles [fd 1]"
    val expected = "AgentSet.ask(world.turtles(), false, function(){ Prims.fd(1) });"
    assertResult(expected)(compile(input))
  }

  test("commands: ask with turtle variable") {
    import Compiler.{compileCommands => compile}
    val input = "__ask-sorted turtles [output-print xcor]"
    val expected = "AgentSet.ask(world.turtles(), false, function(){ println(AgentSet.getTurtleVariable(3)) });"
    assertResult(expected)(compile(input))
  }

  test("commands: die") {
    import Compiler.{compileCommands => compile}
    val input = "__ask-sorted turtles [die]"
    val expected = "AgentSet.ask(world.turtles(), false, function(){ AgentSet.die() });"
    assertResult(expected)(compile(input))
  }

  test("commands: ask patches with variable") {
    import Compiler.{compileCommands => compile}
    val input = "__ask-sorted patches [output-print pxcor]"
    val expected = "AgentSet.ask(world.patches(), false, function(){ println(AgentSet.getPatchVariable(0)) });"
    assertResult(expected)(compile(input))
  }

  test("globals: access") {
    import Compiler.{compileProcedures => compile}
    val input = "globals [x y z] to foo-bar? output-print z output-print y output-print x end"
    val expected =
     """|Globals.init(3)
        |world = new World(0, 0, 0, 0);
        |function FOO_BAR_P () {
        |println(Globals.getGlobal(2))
        |println(Globals.getGlobal(1))
        |println(Globals.getGlobal(0))
        |};""".stripMargin
    assertResult(expected)(compile(input)._1)
  }

  test("globals: set") {
    import Compiler.{compileProcedures => compile}
    val input = "globals [x] to foo set x 5 output-print x end"
    val expected =
     """|Globals.init(1)
        |world = new World(0, 0, 0, 0);
        |function FOO () {
        |Globals.setGlobal(0,5)
        |println(Globals.getGlobal(0))
        |};""".stripMargin
    assertResult(expected)(compile(input)._1)
  }

  test("commands: ask turtles to set color") {
    import Compiler.{compileCommands => compile}
    val input = "__ask-sorted turtles [set color green]"
    val expected = "AgentSet.ask(world.turtles(), false, function(){ AgentSet.setTurtleVariable(1,55) });"
    assertResult(expected)(compile(input))
  }

  test("commands: ask turtles to set pcolor") {
    import Compiler.{compileCommands => compile}
    val input = "__ask-sorted turtles [set pcolor green]"
    val expected = "AgentSet.ask(world.turtles(), false, function(){ AgentSet.setPatchVariable(2,55) });"
    assertResult(expected)(compile(input))
  }

  test("commands: ask patches to set pcolor") {
    import Compiler.{compileCommands => compile}
    val input = "__ask-sorted patches [set pcolor green]"
    val expected = "AgentSet.ask(world.patches(), false, function(){ AgentSet.setPatchVariable(2,55) });"
    assertResult(expected)(compile(input))
  }

  test("commands: with") {
    import Compiler.{compileCommands => compile}
    val input = "__ask-sorted patches with [pxcor = 1] [output-print pycor]"
    val expectedAgentFilter =
      "AgentSet.agentFilter(world.patches(), function(){ return (AgentSet.getPatchVariable(0) === 1) })"
    val expected = s"AgentSet.ask($expectedAgentFilter, false, function(){ println(AgentSet.getPatchVariable(1)) });"
    assertResult(expected)(compile(input))
  }

}
