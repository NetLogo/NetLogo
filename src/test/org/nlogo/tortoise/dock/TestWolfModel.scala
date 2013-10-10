// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

import org.nlogo.api

class TestWolfModel extends DockingSuite {

  test("wolf") { implicit fixture => import fixture._
    open("models/test/tortoise/Wolf Sheep Predation.nlogo")
    testCommand("setup")
    testCommand("repeat 20 [ go ]")
    testCommand("output-print count wolves output-print count sheep output-print grass")
  }

  test("wolf real") { implicit fixture => import fixture._
    open("models/Sample Models/Biology/Wolf Sheep Predation.nlogo")
    testCommand("setup")
    testCommand("repeat 1 [ go ]")
    testCommand("output-print count wolves output-print count sheep output-print grass")
  }
}
