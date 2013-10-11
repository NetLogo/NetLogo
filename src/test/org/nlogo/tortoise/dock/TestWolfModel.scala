// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

import org.nlogo.api

class TestWolfModel extends DockingSuite {
  test("wolf") { implicit fixture => import fixture._
    open("models/Sample Models/Biology/Wolf Sheep Predation.nlogo")
    testCommand("setup")
    testCommand("repeat 30 [ go ]")
    testCommand("output-print count wolves output-print count sheep output-print grass")
  }
}
