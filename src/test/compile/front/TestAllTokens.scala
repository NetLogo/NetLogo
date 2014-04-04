// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package front

import org.scalatest.FunSuite
import org.nlogo.nvm
import FrontEnd.tokenMapper._

class TestAllTokens extends FunSuite {
  test("all listed primitives exist") {
    for (className <- FrontEnd.tokenMapper.allClassNames)
      Instantiator.newInstance[nvm.Instruction](Class.forName(className))
  }
}
