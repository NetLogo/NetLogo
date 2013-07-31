// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.scalatest.FunSuite

class TestAllTokens extends FunSuite {
  test("all listed primitives exist") {
    Parser.tokenMapper.checkInstructionMaps()
  }
}
