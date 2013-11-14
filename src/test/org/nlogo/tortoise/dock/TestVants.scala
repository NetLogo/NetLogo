// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

import org.nlogo.util.SlowTest

class TestVantsModel extends DockingSuite with SlowTest {

  test("vants") { implicit fixture => import fixture._
    open("models/test/tortoise/Vants.nlogo")
    testCommand("setup")
    for (_ <- 1 to 10)
      testCommand("go-forward")
    for (_ <- 1 to 10)
      testCommand("go-reverse")
  }

}
