// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

import org.nlogo.api.WorldDimensions

class TestLifeModel extends DockingSuite {

  test("life") { implicit fixture => import fixture._
    val lifeSrc =
      """
        |patches-own [ living? live-neighbors ]
        |
        |to setup
        |  clear-all
        |  ask patches [ cell-death ]
        |  ask patch  0  0 [ cell-birth ]
        |  ask patch -1  0 [ cell-birth ]
        |  ask patch  0 -1 [ cell-birth ]
        |  ask patch  0  1 [ cell-birth ]
        |  ask patch  1  1 [ cell-birth ]
        |  reset-ticks
        |end
        |
        |to cell-birth set living? true  set pcolor white end
        |to cell-death set living? false set pcolor black end
        |
        |to go
        |  ask patches [
        |    set live-neighbors count neighbors with [living?] ]
        |  ask patches [
        |    ifelse live-neighbors = 3
        |      [ cell-birth ]
        |      [ if live-neighbors != 2
        |        [ cell-death ] ] ]
        |  tick
        |end
      """.stripMargin
    declare(lifeSrc, WorldDimensions.square(5))
    testCommand("setup")
    for (_ <- 1 to 5)
      testCommand("go")
    testCommand("""__ask-sorted patches [output-print (word self " -> " living?) ]""")
  }

}
