// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

import org.nlogo.api

class TestClimateModel extends DockingSuite {

  test("climate") { implicit fixture => import fixture._
    open("models/test/tortoise/Climate Change.nlogo")
    testCommand("setup")
    testCommand("add-cloud")
    testCommand("add-cloud")
    for (_ <- 1 to 10)
      testCommand("add-CO2")
    testCommand("remove-cloud")
    testCommand("add-cloud")
    for (_ <- 1 to 10) {
      testCommand("add-CO2")
      testCommand("go")
      testCommand("remove-CO2")
    }
    testCommand("remove-cloud")
    // unfortunately this takes quite a while on Rhino, but it isn't a good test unless we run the
    // model for long enough for the turtles to start interacting with each other and with the world
    // boundaries - ST 10/10/13
    for (_ <- 1 to 4)
      testCommand("repeat 50 [ go ]")
    compare("temperature")
  }
}
