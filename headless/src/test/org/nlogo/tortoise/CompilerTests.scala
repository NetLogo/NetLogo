// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise

import org.scalatest.FunSuite

class TestCompiler extends FunSuite {

  test("literals") {
    import Compiler.{compileReporter => compile}
    expectResult("1")(
      compile("1"))
    expectResult("1")(
      compile("1.0"))
    expectResult("[]")(
      compile("[]"))
    expectResult("[1, [2], 3]")(
      compile("[1 [2] 3]"))
  }

  test("arithmetic expressions") {
    import Compiler.{compileReporter => compile}
    expectResult("(2 + 2)")(
      compile("2 + 2"))
    expectResult("((1 + 2) * 3)")(
      compile("(1 + 2) * 3"))
    expectResult("(1 + (2 * 3))")(
      compile("1 + 2 * 3"))
    expectResult("((1 + 2) + (3 + 4))")(
      compile("(1 + 2) + (3 + 4)"))
  }

  test("equality"){
    import Compiler.{compileReporter => compile}
    expectResult("(2 === 2)")(compile("2 = 2"))
    expectResult("""("hello" === "hello")""")(compile(""""hello" = "hello""""))
  }

  test("commands: arithmetic + printing") {
    import Compiler.{compileCommands => compile}
    val expected = """|println((2 + 2))
                      |println((3 * 3))""".stripMargin
    expectResult(expected)(
      compile("output-print 2 + 2 output-print 3 * 3"))
  }

  test("commands: turtle creation") {
    import Compiler.{compileCommands => compile}
    val expected = """|world.createorderedturtles(5)
                      |println(AgentSet.count(world.turtles()))""".stripMargin
    expectResult(expected)(
      compile("cro 5 output-print count turtles"))
  }

  test("commands: while true") {
    import Compiler.{compileCommands => compile}
    val input = "while [true] [cro 1]"
    val expected =
      """while (true) {
        |world.createorderedturtles(1)
        |}""".stripMargin
    expectResult(expected)(compile(input))
  }

  test("commands: let") {
    import Compiler.{compileCommands => compile}
    val input = "let x 5 output-print x"
    val expected = """|var X = 5;
                      |println(X)""".stripMargin
    expectResult(expected)(compile(input))
  }

  test("command procedure") {
    import Compiler.{compileProcedures => compile}
    val input = "to foo output-print 5 end"
    val expected = """world = new World(0, 0, 0, 0);
                     |function FOO () {
                     |println(5)
                     |};""".stripMargin
    expectResult(expected)(compile(input)._1)
  }

  test("commands: ask simple") {
    import Compiler.{compileCommands => compile}
    val input = "__ask-sorted turtles [fd 1]"
    val expected = "AgentSet.ask(world.turtles(), function(){ Prims.fd(1) })"
    expectResult(expected)(compile(input))
  }

  test("commands: ask with turtle variable") {
    import Compiler.{compileCommands => compile}
    val input = "__ask-sorted turtles [output-print xcor]"
    val expected = "AgentSet.ask(world.turtles(), function(){ println(AgentSet.getTurtleVariable(3)) })"
    expectResult(expected)(compile(input))
  }

  test("commands: die") {
    import Compiler.{compileCommands => compile}
    val input = "__ask-sorted turtles [die]"
    val expected = "AgentSet.ask(world.turtles(), function(){ AgentSet.die() })"
    expectResult(expected)(compile(input))
  }

  test("commands: ask patches with variable") {
    import Compiler.{compileCommands => compile}
    val input = "__ask-sorted patches [output-print pxcor]"
    val expected = "AgentSet.ask(world.patches(), function(){ println(AgentSet.getPatchVariable(0)) })"
    expectResult(expected)(compile(input))
  }

  test("globals: access") {
    import Compiler.{compileProcedures => compile}
    val input = "globals [x y z] to foo output-print z output-print y output-print x end"
    val expected =
     """|Globals.init(3)
        |world = new World(0, 0, 0, 0);
        |function FOO () {
        |println(Globals.getGlobal(2))
        |println(Globals.getGlobal(1))
        |println(Globals.getGlobal(0))
        |};""".stripMargin
    expectResult(expected)(compile(input)._1)
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
    expectResult(expected)(compile(input)._1)
  }

  test("commands: ask turtles to set color") {
    import Compiler.{compileCommands => compile}
    val input = "__ask-sorted turtles [set color green]"
    val expected = "AgentSet.ask(world.turtles(), function(){ AgentSet.setTurtleVariable(1,55) })"
    expectResult(expected)(compile(input))
  }

  test("commands: ask turtles to set pcolor") {
    import Compiler.{compileCommands => compile}
    val input = "__ask-sorted turtles [set pcolor green]"
    val expected = "AgentSet.ask(world.turtles(), function(){ AgentSet.setPatchVariable(2,55) })"
    expectResult(expected)(compile(input))
  }

  test("commands: ask patches to set pcolor") {
    import Compiler.{compileCommands => compile}
    val input = "__ask-sorted patches [set pcolor green]"
    val expected = "AgentSet.ask(world.patches(), function(){ AgentSet.setPatchVariable(2,55) })"
    expectResult(expected)(compile(input))
  }

  test("commands: with") {
    import Compiler.{compileCommands => compile}
    val input = "__ask-sorted patches with [pxcor = 1] [output-print pycor]"
    val expectedAgentFilter =
      "AgentSet.agentFilter(world.patches(), function(){ return (AgentSet.getPatchVariable(0) === 1) })"
    val expected = s"AgentSet.ask($expectedAgentFilter, function(){ println(AgentSet.getPatchVariable(1)) })"
    expectResult(expected)(compile(input))
  }
}
