// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise

import org.scalatest.FunSuite

class TestCompiler extends FunSuite {

  import Compiler.compile

  test("literals") {
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
    expectResult("(2) + (2)")(
      compile("2 + 2"))
    expectResult("((1) + (2)) + (3)")(
      compile("1 + 2 + 3"))
    expectResult("(1) + ((2) + (3))")(
      compile("1 + (2 + 3)"))
    expectResult("((1) + (2)) + ((3) + (4))")(
      compile("(1 + 2) + (3 + 4)"))
  }

}
