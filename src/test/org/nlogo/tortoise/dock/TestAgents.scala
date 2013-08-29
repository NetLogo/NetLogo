// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

import org.nlogo.api.WorldDimensions

class TestAgents extends DockingSuite {

  tester("turtle creation") {
    defineProcedures("")
    compareCommands("output-print count turtles")
    compareCommands("cro 1")
    compareCommands("output-print count turtles")
    compareCommands("cro 4")
    compareCommands("output-print count turtles")
    compareCommands("clear-all")
    compareCommands("output-print count turtles")
  }

  tester("crt") {
    defineProcedures("")
    compareCommands("random-seed 0 crt 10")
    compareCommands("__ask-sorted turtles [ output-print color output-print heading ]")
  }

  tester("random-xcor/ycor") {
    defineProcedures("")
    compareCommands("cro 10")
    compareCommands("random-seed 0 __ask-sorted turtles [ setxy random-xcor random-ycor ]")
  }

  tester("ask") {
    defineProcedures("")
    compareCommands("cro 3")
    compareCommands("__ask-sorted turtles [ output-print 0 ]")
  }

  tester("turtle motion 1") {
    defineProcedures("", WorldDimensions.square(1))
    compareCommands("cro 4 __ask-sorted turtles [fd 1] __ask-sorted turtles [output-print xcor output-print ycor]")
  }

  tester("turtle motion 2") {
    defineProcedures("", WorldDimensions.square(1))
    compareCommands("cro 8 __ask-sorted turtles [fd 1] __ask-sorted turtles [output-print xcor output-print ycor]")
  }

  tester("turtle death") {
    defineProcedures("")
    compareCommands("cro 8")
    compareCommands("__ask-sorted turtles [die]")
    compareCommands("__ask-sorted turtles [output-print xcor]")
  }

  tester("turtle size") {
    defineProcedures("")
    compareCommands("cro 1 __ask-sorted turtles [ set size 5 ]")
    compareCommands("__ask-sorted turtles [ output-print size ]")
  }

  tester("turtle color") {
    defineProcedures("")
    compareCommands("cro 1 __ask-sorted turtles [ set color blue ]")
    compareCommands("__ask-sorted turtles [ output-print blue ]")
  }

  tester("patches") {
    defineProcedures("")
    compareCommands("__ask-sorted patches [output-print pxcor]")
  }

  tester("patch variables") {
    val src =
      """
        |patches-own [ living? live-neighbors ]
        |to cellbirth set living? true  set pcolor white end
        |to celldeath set living? false set pcolor black end
      """.stripMargin
    defineProcedures(src)
    compareCommands("__ask-sorted patches [cellbirth output-print living?]")
    compareCommands("__ask-sorted patches [celldeath output-print living?]")
  }

  tester("patch order") {
    defineProcedures("", WorldDimensions.square(5))
    compareCommands("""__ask-sorted patches [ output-print self ]""")
  }

  tester("turtles get patch variables") {
    defineProcedures("", WorldDimensions.square(5))
    compareCommands("cro 5 __ask-sorted turtles [ fd 1 ]")
    compareCommands("""__ask-sorted turtles [ output-print self ]""")
  }

  tester("turtles set patch variables") {
    defineProcedures("", WorldDimensions.square(5))
    compareCommands("cro 5 __ask-sorted turtles [ fd 1 set pcolor blue ]")
    compareCommands("__ask-sorted turtles [output-print color]")
    compareCommands("__ask-sorted turtles [output-print pcolor]")
    compareCommands("__ask-sorted patches [output-print pcolor]")
  }

  tester("with") {
    defineProcedures("", WorldDimensions.square(5))
    compareCommands("__ask-sorted patches with [pxcor = 1] [output-print pycor]")
  }

  tester("with 2") {
    defineProcedures("", WorldDimensions.square(5))
    compareCommands("__ask-sorted patches with [pxcor = -3 and pycor = 2] [ output-print self ]")
  }

  tester("with + turtles accessing turtle and patch vars") {
    defineProcedures("", WorldDimensions.square(5))
    compareCommands("cro 5 ask turtles [fd 1]")
    compareCommands("__ask-sorted turtles with [pxcor =  1] [output-print pycor]")
    compareCommands("__ask-sorted turtles with [pxcor = -1] [output-print ycor]")
  }

  tester("get patch") {
    defineProcedures("")
    compareCommands("output-print patch 0 0")
  }

  tester("get turtle") {
    defineProcedures("")
    compareCommands("cro 5")
    compareCommands("__ask-sorted turtles [ output-print self ]")
  }

  tester("patch set") {
    defineProcedures("", WorldDimensions.square(5))
    compareCommands("__ask-sorted patches with [pxcor = -1 and pycor = 0] [ set pcolor green ]")
    compareCommands("ask patch 0 0 [ set pcolor green ]")
    compareCommands("output-print count patches with [pcolor = green]")
  }

  tester("and, or") {
    defineProcedures("", WorldDimensions.square(5))
    compareCommands("output-print count patches with [pxcor = 0 or pycor = 0]")
    compareCommands("output-print count patches with [pxcor = 0 and pycor = 0]")
  }

//  tester("neighbors") {
//    defineProcedures("", WorldDimensions.square(5))
//    compareCommands("""__ask-sorted patches [ __ask-sorted neighbors [ output-print self ]]""")
//  }

  tester("setting a built-in patch variable") {
    defineProcedures("", WorldDimensions.square(5))
    compareCommands("__ask-sorted patches with [pxcor = 2 and pycor = 3] [ set pcolor green ]")
    compareCommands("output-print count patches with [pcolor = green]")
    compareCommands("__ask-sorted patches [ output-print self output-print pcolor ]")
  }

  tester("setting a patches-own variable") {
    defineProcedures("patches-own [foo]", WorldDimensions.square(5))
    compareCommands("__ask-sorted patches with [pxcor = 2 and pycor = 3] [ set foo green ]")
    compareCommands("output-print count patches with [foo = green]")
    compareCommands("__ask-sorted patches [ output-print self output-print foo ]")
  }

  tester("clear-all clears patches") {
    defineProcedures("patches-own [p]")
    compareCommands("ask patches [ set p 123 ]")
    compareCommands("ask patches [ set pcolor green ]")
    compareCommands("clear-all")
    compareCommands("output-print count patches with [pcolor = green]")
  }

  tester("sprout") {
    defineProcedures("")
    compareCommands("random-seed 0 " +
      "__ask-sorted patches with [pxcor >= 0] [ sprout 1 ]")
  }

  tester("turtle motion") {
    defineProcedures("", WorldDimensions.square(5))
    compareCommands("random-seed 0 crt 100")
    compareCommands("__ask-sorted turtles [ setxy random-xcor random-ycor ]")
    for (_ <- 1 to 10)
      compareCommands("__ask-sorted turtles [ fd 1 ]")
  }

}
