// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

import org.nlogo.util.SlowTest

class TestWolfModel extends DockingSuite with SlowTest {

  test("wolf no grass") { implicit fixture => import fixture._
    open("models/Sample Models/Biology/Wolf Sheep Predation.nlogo")
    testCommand("set grass? false")
    testCommand("setup")
    testCommand("repeat 40 [ go ]")
    testCommand("output-print count wolves output-print count sheep output-print grass")
  }

  test("wolf grass") { implicit fixture => import fixture._
    open("models/Sample Models/Biology/Wolf Sheep Predation.nlogo")
    testCommand("set grass? true")
    testCommand("setup")
    testCommand("repeat 10 [ go ]")
    testCommand("output-print count wolves output-print count sheep output-print grass")
  }

}
