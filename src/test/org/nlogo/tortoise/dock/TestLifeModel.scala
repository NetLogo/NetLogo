// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

import org.nlogo.api.WorldDimensions

class TestLifeModel extends DockingSuite {

  tester("life") {
    val lifeSrc =
      """
        |patches-own [ living? live-neighbors ]
        |
        |to setup
        |  clear-all
        |  ask patches [ celldeath ]
        |  ask patch  0  0 [ cellbirth ]
        |  ask patch -1  0 [ cellbirth ]
        |  ask patch  0 -1 [ cellbirth ]
        |  ask patch  0  1 [ cellbirth ]
        |  ask patch  1  1 [ cellbirth ]
        |end
        |
        |to cellbirth set living? true  set pcolor white end
        |to celldeath set living? false set pcolor black end
        |
        |to go
        |  ask patches [
        |    set live-neighbors count neighbors with [living?] ]
        |  ask patches [
        |    ifelse live-neighbors = 3
        |      [ cellbirth ]
        |      [ if live-neighbors != 2
        |        [ celldeath ] ] ]
        |end
      """.stripMargin
    defineProcedures(lifeSrc, WorldDimensions.square(5))
    compareCommands("setup")
    for (_ <- 1 to 5)
      compareCommands("go")
    compareCommands("""__ask-sorted patches [output-print (word self " -> " living?) ]""")
  }

}
