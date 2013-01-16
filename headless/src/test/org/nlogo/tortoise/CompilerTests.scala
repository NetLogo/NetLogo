// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise

import org.scalatest.FunSuite

class TestCompiler extends FunSuite {

  test("compile") {
    import Compiler.compile
    expectResult("1")(compile("1"))
    expectResult("1")(compile("1.0"))
    expectResult("[]")(compile("[]"))
    expectResult("[1, [2], 3]")(compile("[1 [2] 3]"))
  }

}
