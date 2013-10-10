// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

import org.nlogo.api, api.WorldDimensions

class TestAgents extends DockingSuite {

  test("world dimensions") { implicit fixture => import fixture._
    declare("", WorldDimensions.square(2))
    compare("count patches")
  }

  test("turtle creation 1") { implicit fixture => import fixture._
    testCommand("cro 2")
    testCommand("cro 2")
  }

  test("turtle creation 2") { implicit fixture => import fixture._
    testCommand("output-print count turtles")
    testCommand("cro 1")
    testCommand("output-print count turtles")
    testCommand("cro 4")
    testCommand("output-print count turtles")
    testCommand("clear-all")
    testCommand("output-print count turtles")
  }

  test("cro with init block") { implicit fixture => import fixture._
    testCommand("crt 4 [ output-print who ]")
  }

  test("crt with init block") { implicit fixture => import fixture._
    testCommand("crt 4 [ output-print color output-print heading ]")
  }

  test("random-xcor/ycor") { implicit fixture => import fixture._
    testCommand("cro 10 [ setxy random-xcor random-ycor ]")
  }

  test("ask") { implicit fixture => import fixture._
    testCommand("cro 3")
    testCommand("ask turtles [ output-print 0 ]")
  }

  test("turtle motion 1") { implicit fixture => import fixture._
    declare("", WorldDimensions.square(1))
    testCommand("cro 4 ask turtles [fd 1] ask turtles [output-print xcor output-print ycor]")
  }

  test("turtle motion 2") { implicit fixture => import fixture._
    declare("", WorldDimensions.square(1))
    testCommand("cro 8 ask turtles [fd 1] ask turtles [output-print xcor output-print ycor]")
  }

  test("turtle death 1") { implicit fixture => import fixture._
    testCommand("cro 8")
    testCommand("ask turtles [die]")
    testCommand("ask turtles [output-print xcor]")
  }

  test("turtle death 2") { implicit fixture => import fixture._
    testCommand("cro 5")
    testCommand("ask turtles with [who = 2] [ die ]")
    testCommand("ask turtles [output-print who]")
  }

  test("turtle death stops execution") { implicit fixture => import fixture._
    declare("""| to p1 [] die end
               | to p2 [] output-print "Never here" end
               |""".stripMargin)
    testCommand("cro 5")
    testCommand("ask turtles [ p1 p2 ]")
  }

  test("turtle size") { implicit fixture => import fixture._
    testCommand("cro 1 ask turtles [ set size 5 ]")
    testCommand("ask turtles [ output-print size ]")
  }

  test("turtle color") { implicit fixture => import fixture._
    testCommand("cro 1 ask turtles [ set color blue ]")
    testCommand("ask turtles [ output-print blue ]")
  }

  test("patches") { implicit fixture => import fixture._
    testCommand("ask patches [output-print pxcor]")
  }

  test("patch variables") { implicit fixture => import fixture._
    val src =
      """
        |patches-own [ living? live-neighbors ]
        |to cell-birth set living? true  set pcolor white end
        |to cell-death set living? false set pcolor black end
      """.stripMargin
    declare(src)
    testCommand("ask patches [cell-birth output-print living?]")
    testCommand("ask patches [cell-death output-print living?]")
  }

  test("patch order") { implicit fixture => import fixture._
    declare("", WorldDimensions.square(5))
    testCommand("""ask patches [ output-print self ]""")
  }

  test("turtles get patch variables") { implicit fixture => import fixture._
    declare("", WorldDimensions.square(5))
    testCommand("cro 5 ask turtles [ fd 1 ]")
    testCommand("""ask turtles [ output-print self ]""")
  }

  test("turtles set patch variables") { implicit fixture => import fixture._
    declare("", WorldDimensions.square(5))
    testCommand("cro 5 ask turtles [ fd 1 set pcolor blue ]")
    testCommand("ask turtles [output-print color]")
    testCommand("ask turtles [output-print pcolor]")
    testCommand("ask patches [output-print pcolor]")
  }

  test("with 1") { implicit fixture => import fixture._
    declare("", WorldDimensions.square(5))
    testCommand("ask patches with [true] [output-print pycor]")
  }

  test("with 1b") { implicit fixture => import fixture._
    declare("", WorldDimensions.square(5))
    testCommand("ask patches with [pcolor = black] [output-print pycor]")
  }

  test("with 2a") { implicit fixture => import fixture._
    declare("", WorldDimensions.square(5))
    testCommand("ask patches with [pxcor > 0 and pxcor < 2] [output-print pycor]")
  }

  test("with 2") { implicit fixture => import fixture._
    declare("", WorldDimensions.square(5))
    testCommand("ask patches with [pxcor = 1] [output-print pycor]")
  }

  test("with 3") { implicit fixture => import fixture._
    declare("", WorldDimensions.square(5))
    testCommand("ask patches with [pxcor = -3 and pycor = 2] [ output-print self ]")
  }

  test("with + turtles accessing turtle and patch vars") { implicit fixture => import fixture._
    declare("", WorldDimensions.square(5))
    testCommand("cro 5 ask turtles [fd 1]")
    testCommand("ask turtles with [pxcor =  1] [output-print pycor]")
    testCommand("ask turtles with [pxcor = -1] [output-print ycor]")
  }

  test("of") { implicit fixture => import fixture._
    declare("", WorldDimensions.square(0))
    testCommand("cro 5")
    testCommand("output-print sum [who] of turtles")
    testCommand("output-print [ pxcor ] of patch 0 0")
  }

  test("one-of") { implicit fixture => import fixture._
    declare("", WorldDimensions.square(0))
    testCommand("output-print one-of patches")
    testCommand("output-print one-of turtles")
    testCommand("create-turtles 2")
    testCommand("output-print one-of turtles")
  }

  test("get patch") { implicit fixture => import fixture._
    testCommand("output-print patch 0 0")
  }

  test("get turtle") { implicit fixture => import fixture._
    testCommand("cro 5")
    testCommand("ask turtles [ output-print self ]")
    testCommand("output-print turtle 1")
    testCommand("output-print turtle 8")
  }

  test("patch set") { implicit fixture => import fixture._
    declare("", WorldDimensions.square(5))
    testCommand("ask patches with [pxcor = -1 and pycor = 0] [ set pcolor green ]")
    testCommand("ask patch 0 0 [ set pcolor green ]")
    testCommand("output-print count patches with [pcolor = green]")
  }

  test("any") { implicit fixture => import fixture._
    declare("", WorldDimensions.square(0))
    testCommand("output-print any? patches")
    testCommand("output-print any? turtles")
    testCommand("cro 1")
    testCommand("output-print any? turtles")
  }

  test("dimensions") { implicit fixture => import fixture._
    declare("", WorldDimensions(-1, 2, -3, 4))
    testCommand("output-print min-pxcor")
  }

  test("and, or") { implicit fixture => import fixture._
    declare("", WorldDimensions.square(5))
    testCommand("output-print count patches with [pxcor = 0 or pycor = 0]")
    testCommand("output-print count patches with [pxcor = 0 and pycor = 0]")
  }

  test("neighbors") { implicit fixture => import fixture._
    declare("", WorldDimensions.square(5))
    testCommand("""ask patches [ ask neighbors [ output-print self ]]""")
  }

  test("setting a built-in patch variable") { implicit fixture => import fixture._
    declare("", WorldDimensions.square(5))
    testCommand("ask patches with [pxcor = 2 and pycor = 3] [ set pcolor green ]")
    testCommand("output-print count patches with [pcolor = green]")
    testCommand("ask patches [ output-print self output-print pcolor ]")
  }

  test("setting a patches-own variable") { implicit fixture => import fixture._
    declare("patches-own [foo]", WorldDimensions.square(5))
    testCommand("ask patches with [pxcor = 2 and pycor = 3] [ set foo green ]")
    testCommand("output-print count patches with [foo = green]")
    testCommand("ask patches [ output-print self output-print foo ]")
  }

  test("clear-all clears patches") { implicit fixture => import fixture._
    declare("patches-own [p]")
    testCommand("ask patches [ set p 123 ]")
    testCommand("ask patches [ set pcolor green ]")
    testCommand("clear-all")
    testCommand("output-print count patches with [pcolor = green]")
  }

  test("sprout 1") { implicit fixture => import fixture._
    testCommand("ask patches with [pxcor >= 0] [ sprout 1 ]")
  }

  test("sprout 2") { implicit fixture => import fixture._
    testCommand("ask patches [ sprout 1 [ set color red ] ]")
  }

  test("turtle motion") { implicit fixture => import fixture._
    declare("", WorldDimensions.square(5))
    testCommand("crt 100")
    testCommand("ask turtles [ setxy random-xcor random-ycor ]")
    for (_ <- 1 to 10)
      testCommand("ask turtles [ fd 1 ]")
  }

  test("patch-ahead") { implicit fixture => import fixture._
    declare("", WorldDimensions.square(2))
    testCommand("cro 7")
    testCommand("ask turtles [ output-print patch-ahead 1 ]")
    testCommand("ask turtles [ output-print patch-ahead 100 ]")
  }

  test("turtles-here") { implicit fixture => import fixture._
    declare("", WorldDimensions.square(2))
    testCommand("ask patches [ sprout 2 ]")
    compare("sum [count turtles-here] of turtles")
  }

  test("set heading negative") { implicit fixture => import fixture._
    // should get normalized to 260
    testCommand("crt 1 [ set heading -100 ]")
  }

  test("shape downcasing") { implicit fixture => import fixture._
    workspace.world.turtleShapeList.add(new api.DummyShape("turtle"))
    testCommand("""crt 1 [ set shape "TURTLE" output-print shape ]""")
  }

  // TODO currently failing, haven't figured out why yet - ST 9/20/13
  // test("turtle creation + procedure call") { implicit fixture => import fixture._
  //   declare("to foo cro 2 end")
  //   testCommand("foo")
  // }

}
