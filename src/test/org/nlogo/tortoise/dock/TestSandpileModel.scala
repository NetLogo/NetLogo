// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

import org.nlogo.api

class TestSandpileModel extends DockingSuite {

  test("sandpile") { implicit fixture => import fixture._
    open("models/test/tortoise/Sandpile.nlogo")
    // TODO next line shouldn't be necessary, but WidgetParser mysteriously lacks
    // chooser support; it's NetLogo/NetLogo#456 - ST 10/11/13
    testCommand("""set drop-location "center"""")
    testCommand("setup-random")
    compare("drop-location")
    compare("(word drop-patch)")
    // this takes a while on Rhino, but it probably isn't a good test unless we run the
    // model for long enough for good avalanches to get going - ST 10/10/13
    for (_ <- 1 to 100)
      testCommand("go")
    compare("total")
    compare("sizes")
    compare("lifetimes")
  }

}
