// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package front

import org.scalatest.FunSuite
import org.nlogo.nvm

class TestAllTokens extends FunSuite {
  test("all listed primitives exist") {
    for (className <- TokenMapper.allClassNames)
      Instantiator.newInstance[nvm.Instruction](Class.forName(className))
  }
}
