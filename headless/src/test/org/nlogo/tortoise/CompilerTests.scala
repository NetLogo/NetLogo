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
    expectResult("(2) + (2)")(
      compile("2 + 2"))
    expectResult("((1) + (2)) + (3)")(
      compile("1 + 2 + 3"))
    expectResult("(1) + ((2) + (3))")(
      compile("1 + (2 + 3)"))
    expectResult("((1) + (2)) + ((3) + (4))")(
      compile("(1 + 2) + (3 + 4)"))
  }

  test("commands") {
    import Compiler.{compileCommands => compile}
    val expected = """|(function () {
                      |println((2) + (2));
                      |return;}).call(this);""".stripMargin
    expectResult(expected)(
      compile("output-print 2 + 2"))
  }

}
