// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

import org.nlogo.util.SlowTest

class TestVotingModel extends DockingSuite with SlowTest {

  test("voting") { implicit fixture => import fixture._
    open("models/Sample Models/Social Science/Voting.nlogo")
    testCommand("resize-world -10 10 -10 10")
    testCommand("setup")
    for (_ <- 1 to 10)
      testCommand("go")
  }

}
