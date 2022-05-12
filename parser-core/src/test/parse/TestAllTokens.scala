// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.scalatest.funsuite.AnyFunSuite

class TestAllTokens extends AnyFunSuite {
  test("all listed primitives exist") {
    FrontEnd.tokenMapper.checkInstructionMaps()
  }
}
