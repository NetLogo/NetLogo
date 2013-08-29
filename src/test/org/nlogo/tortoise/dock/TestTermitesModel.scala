// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

class TestTermitesModel extends DockingSuite {

  tester("termites") {
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
       |    [ searchforchip ]
       |    [ ifelse next = 2
       |      [ findnewpile ]
       |      [ ifelse next = 3
       |        [ putdownchip ]
       |        [ getaway ] ] ]
       |end
       |
       |to searchforchip
       |  if pcolor = yellow
       |    [ set pcolor black
       |      set color orange
       |      set steps 20
       |      set next 2 ]
       |end
       |
       |to findnewpile
       |  if pcolor = yellow
       |    [ set next 3 ]
       |end
       |
       |to putdownchip
       |  if pcolor = black
       |   [ set pcolor yellow
       |     set color white
       |     set steps 20
       |     set next 4 ]
       |end
       |
       |to getaway
       |  if pcolor = black
       |    [ set next 1 ]
       |end
      """.stripMargin
    defineProcedures(code, -20, 20, -20, 20)
    compareCommands("random-seed 0 setup")
    for (_ <- 1 to 20)
      compareCommands("go")
  }

}
