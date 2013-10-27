// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

import org.nlogo.util.SlowTest

class TestRopeModel extends DockingSuite with SlowTest {

  test("rope") { implicit fixture => import fixture._
    open("models/Sample Models/Chemistry & Physics/Waves/Rope.nlogo")
    testCommand("setup")
    for (_ <- 1 to 10)
      testCommand("go")
  }

}
