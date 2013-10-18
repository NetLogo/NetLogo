// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

import org.nlogo.util.SlowTest

class TestLifeModel extends DockingSuite with SlowTest {

  test("life") { implicit fixture => import fixture._
    open("models/test/tortoise/Life Simple.nlogo")
    testCommand("setup")
    for (_ <- 1 to 30)
      testCommand("go")
    testCommand("""ask patches [output-print (word self " -> " living?) ]""")
  }

}
