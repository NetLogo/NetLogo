// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

import org.nlogo.api.WorldDimensions

class TestTermitesModel extends DockingSuite {

  test("termites") { implicit fixture => import fixture._
    open("models/test/tortoise/Termites.nlogo")
    testCommand("setup")
    for (_ <- 1 to 20)
      testCommand("go")
  }

}
