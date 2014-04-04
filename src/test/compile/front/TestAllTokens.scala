// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package front

import org.scalatest.FunSuite
import org.nlogo.nvm
import FrontEnd.tokenMapper._

class TestAllTokens extends FunSuite {
  test("all listed commands exist") {
    for (className <- FrontEnd.tokenMapper.allCommandClassNames)
      Instantiator.newInstance[nvm.Command](Class.forName(className))
  }
  test("all listed reporters exist") {
    for (className <- FrontEnd.tokenMapper.allReporterClassNames)
      Instantiator.newInstance[nvm.Reporter](Class.forName(className))
  }
}
