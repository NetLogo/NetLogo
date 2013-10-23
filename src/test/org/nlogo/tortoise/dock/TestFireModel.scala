// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

import org.nlogo.util.SlowTest

class TestFireModel extends DockingSuite with SlowTest {
  test("fire") { implicit fixture => import fixture._
    open("models/Sample Models/Earth Science/Fire.nlogo")
    testCommand("setup")
    for (_ <- 1 to 10)
      testCommand("go")
  }
}
