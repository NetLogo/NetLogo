// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

import org.nlogo.util.SlowTest

class TestSlimeModel extends DockingSuite with SlowTest {

  test("slime") { implicit fixture => import fixture._
    open("models/Sample Models/Biology/Slime.nlogo")
    testCommand("resize-world -10 10 -10 10")
    testCommand("set population 30")
    testCommand("setup")
    for (_ <- 1 to 10)
      testCommand("go")
  }

}
