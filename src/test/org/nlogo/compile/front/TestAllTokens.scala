// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile.front

import org.scalatest.FunSuite

class TestAllTokens extends FunSuite {
  test("all listed primitives exist") {
    FrontEnd.tokenMapper.checkInstructionMaps()
  }
}
