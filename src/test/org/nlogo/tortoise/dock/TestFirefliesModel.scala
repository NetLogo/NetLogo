// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

import org.nlogo.util.SlowTest

class TestFirefliesModel extends DockingSuite with SlowTest {

  test("fireflies") { implicit fixture => import fixture._
    open("models/Sample Models/Biology/Fireflies.nlogo")
    testCommand("resize-world -10 10 -10 10")
    testCommand("set number 150")
    testCommand("setup")
    for (_ <- 1 to 20)
      testCommand("go")
  }

}
