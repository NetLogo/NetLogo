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
    val expected = """function FOO () {
                     |println(5)
                     |};""".stripMargin
    expectResult(expected)(compile(input))
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
    val expected = "AgentSet.ask(world.turtles(), function(){ println(AgentSet.getVariable(3)) })"
    expectResult(expected)(compile(input))
  }

}
