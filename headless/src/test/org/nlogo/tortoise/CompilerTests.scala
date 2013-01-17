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
    val expected = """|(function () {
                      |println((2 + 2));
                      |println((3 * 3));
                      |return;}).call(this);""".stripMargin
    expectResult(expected)(
      compile("output-print 2 + 2 output-print 3 * 3"))
  }

  test("commands: turtle creation") {
    import Compiler.{compileCommands => compile}
    val expected = """|(function () {
                      |World.crofast(5);
                      |println(AgentSet.count(World.turtles()));
                      |return;}).call(this);""".stripMargin
    expectResult(expected)(
      compile("cro 5 output-print count turtles"))
  }

}
