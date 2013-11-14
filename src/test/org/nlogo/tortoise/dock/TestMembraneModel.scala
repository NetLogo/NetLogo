// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

import org.nlogo.util.SlowTest

class TestMembraneModel extends DockingSuite with SlowTest {

  test("membrane") { implicit fixture => import fixture._
    open("models/Sample Models/Biology/Membrane Formation.nlogo")
    testCommand("resize-world -10 10 -10 10 set num-lipids 50 set num-water 150")
    testCommand("set num-lipids 50")
    testCommand("set num-water 50")
    testCommand("setup")
    testCommand("repeat 15 [ go ]")
  }
}
