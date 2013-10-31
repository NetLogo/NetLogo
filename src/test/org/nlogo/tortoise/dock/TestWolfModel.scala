// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

import org.nlogo.util.SlowTest

class TestWolfModel extends DockingSuite with SlowTest {

  // Fails if you run it longer (e.g. 40 ticks instead of 30).  It's probably because we
  // aren't matching turtles-here ordering (which is NetLogo/Tortoise#31) - ST 10/31/13

  test("wolf no grass") { implicit fixture => import fixture._
    open("models/Sample Models/Biology/Wolf Sheep Predation.nlogo")
    testCommand("set grass? false")
    testCommand("setup")
    testCommand("repeat 30 [ go ]")
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
