// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise

import org.scalatest.FunSuite

class TestCompiler extends FunSuite {

  import Compiler.compileReporter

  test("literals") {
    expectResult("1")(
      compileReporter("1"))
    expectResult("1")(
      compileReporter("1.0"))
    expectResult("[]")(
      compileReporter("[]"))
    expectResult("[1, [2], 3]")(
      compileReporter("[1 [2] 3]"))
  }

  test("arithmetic expressions") {
    expectResult("(2) + (2)")(
      compileReporter("2 + 2"))
    expectResult("((1) + (2)) + (3)")(
      compileReporter("1 + 2 + 3"))
    expectResult("(1) + ((2) + (3))")(
      compileReporter("1 + (2 + 3)"))
    expectResult("((1) + (2)) + ((3) + (4))")(
      compileReporter("(1 + 2) + (3 + 4)"))
  }

}
