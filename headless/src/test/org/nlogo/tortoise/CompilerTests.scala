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
    val expected = """|println((2 + 2));
                      |println((3 * 3));""".stripMargin
    expectResult(expected)(
      compile("output-print 2 + 2 output-print 3 * 3"))
  }

  test("commands: turtle creation") {
    import Compiler.{compileCommands => compile}
    val expected = """|World.createorderedturtles(5);
                      |println(AgentSet.count(World.turtles()));""".stripMargin
    expectResult(expected)(
      compile("cro 5 output-print count turtles"))
  }

  test("commands: while true") {
    import Compiler.{compileCommands => compile}
    val input = "while [true] [cro 1]"
    val expected =
      """while (true) {
        |World.createorderedturtles(1);
        |}""".stripMargin
    expectResult(expected)(compile(input))
  }

  test("commands: let") {
    import Compiler.{compileCommands => compile}
    val input = "let x 5 output-print x"
    val expected = """|var X = 5
                      |println(X);""".stripMargin
    expectResult(expected)(compile(input))
  }

}
