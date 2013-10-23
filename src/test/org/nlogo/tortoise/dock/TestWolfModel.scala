// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

import org.nlogo.util.SlowTest

class TestWolfModel extends DockingSuite with SlowTest {
  test("wolf") { implicit fixture => import fixture._
    open("models/Sample Models/Biology/Wolf Sheep Predation.nlogo")
    testCommand("setup")
    testCommand("repeat 30 [ go ]")
    testCommand("output-print count wolves output-print count sheep output-print grass")
  }
}
