// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

import org.nlogo.api.WorldDimensions

class TestTermitesModel extends DockingSuite {

  test("termites") { implicit fixture => import fixture._
    val code =
      """
       |turtles-own [next steps]
       |
       |to setup
       |  clear-all
       |  __ask-sorted patches [
       |    if random 100 < 20
       |      [ set pcolor yellow ] ]
       |  crt 50
       |  __ask-sorted turtles [
       |    set color white
       |    setxy random-xcor random-ycor
       |    set size 3
       |    set next 1
       |  ]
       |end
       |
       |to go
       |  __ask-sorted turtles
       |    [ ifelse steps > 0
       |        [ set steps steps - 1 ]
       |        [ action
       |          wiggle ]
       |      fd 1 ]
       |end
       |
       |to wiggle
       |  rt random 50
       |  lt random 50
       |end
       |
       |to action
       |  ifelse next = 1
       |    [ search-for-chip ]
       |    [ ifelse next = 2
       |      [ find-new-pile ]
       |      [ ifelse next = 3
       |        [ put-down-chip ]
       |        [ get-away ] ] ]
       |end
       |
       |to search-for-chip
       |  if pcolor = yellow
       |    [ set pcolor black
       |      set color orange
       |      set steps 20
       |      set next 2 ]
       |end
       |
       |to find-new-pile
       |  if pcolor = yellow
       |    [ set next 3 ]
       |end
       |
       |to put-down-chip
       |  if pcolor = black
       |   [ set pcolor yellow
       |     set color white
       |     set steps 20
       |     set next 4 ]
       |end
       |
       |to get-away
       |  if pcolor = black
       |    [ set next 1 ]
       |end
      """.stripMargin
    declare(code, WorldDimensions.square(10))
    testCommand("random-seed 0 setup")
    for (_ <- 1 to 20)
      testCommand("go")
  }

}
