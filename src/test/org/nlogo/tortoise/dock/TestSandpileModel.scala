// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

import org.nlogo.util.SlowTest

class TestSandpileModel extends DockingSuite with SlowTest {

  test("sandpile random") { implicit fixture => import fixture._
    open("models/test/tortoise/Sandpile.nlogo")
    testCommand("setup-random")
    // this takes a while on Rhino, but it probably isn't a good test unless we run the
    // model for long enough for good avalanches to get going - ST 10/10/13
    for (_ <- 1 to 100)
      testCommand("go")
    compare("total")
    compare("sizes")
    compare("lifetimes")
  }

  test("sandpile uniform") { implicit fixture => import fixture._
    open("models/test/tortoise/Sandpile.nlogo")
    testCommand("setup-uniform 0")
    for (_ <- 1 to 10)
      testCommand("go")
    compare("total")
    compare("sizes")
    compare("lifetimes")
  }

}
