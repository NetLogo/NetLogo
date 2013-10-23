// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

import org.nlogo.util.SlowTest

class TestBoilingModel extends DockingSuite with SlowTest {

  test("boiling") { implicit fixture => import fixture._
    open("models/Sample Models/Chemistry & Physics/Heat/Boiling.nlogo")
    testCommand("setup")
    for (_ <- 1 to 5)
      testCommand("go")
  }
}
