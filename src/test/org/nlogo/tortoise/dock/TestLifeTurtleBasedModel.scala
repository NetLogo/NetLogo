// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

import org.nlogo.util.SlowTest

class TestLifeTurtleBasedModel extends DockingSuite with SlowTest {

  test("life") { implicit fixture => import fixture._
    open("models/test/tortoise/Life Turtle-Based.nlogo")
    testCommand("resize-world -10 10 -10 10")
    testCommand("setup-random")
    for (_ <- 1 to 10)
      testCommand("go")
  }

}
