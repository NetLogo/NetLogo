// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

import org.nlogo.util.SlowTest

class TestDLASimpleModel extends DockingSuite with SlowTest {

  test("dla simple") { implicit fixture => import fixture._
    open("models/Sample Models/Chemistry & Physics/Diffusion Limited Aggregation/DLA Simple.nlogo")
    testCommand("resize-world -20 20 -20 20")
    testCommand("setup")
    testCommand("set num-particles 100")
    for (_ <- 1 to 25)
      testCommand("go")
  }

}
