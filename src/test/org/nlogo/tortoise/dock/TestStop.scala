// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

class TestStop extends DockingSuite {
  test("basic") { implicit fixture => import fixture._
    declare("""|to a []
               |  stop
               |  output-print "NEVER HERE"
               |end""".stripMargin)
    testCommand("a")
  }

  test("conditional 1") { implicit fixture => import fixture._
    declare("""|to a []
               |  if true [ stop ]
               |  output-print "NEVER HERE"
               |end""".stripMargin)
    testCommand("a")
  }

  test("conditional 2") { implicit fixture => import fixture._
    declare("""|to a []
               |  ifelse true [ output-print "GET HERE" ] [ stop ]
               |  ifelse false [ output-print "GET HERE" ] [ stop ]
               |  output-print "NEVER HERE"
               |end""".stripMargin)
    testCommand("a")
  }

  test("repeater") { implicit fixture => import fixture._
    declare("""|globals [run?]
               |to a []
               |  if run? = 1 [ stop ]
               |  output-print "HERE ONCE"
               |  set run? 1
               |end""".stripMargin)
    testCommand("a")
    testCommand("a")
  }

  test("nested") { implicit fixture => import fixture._
    declare("""| to b []
               |   stop
               |   output-print "NEVER HERE"
               | end
               | to a []
               |   b
               |   output-print "GETS HERE"
               | end""".stripMargin)
    testCommand("a")
  }

  // This test fails because we don't have a context in tortoise that can tell that stop is being run in a reporter vs a command - FD 10/4/2013
  /*test("reporter") { implicit fixture => import fixture._
    declare("""| to-report b []
               |   stop
               |   report 10
               | end
               | to a []
               |   let x b
               | end""".stripMargin)
    testCommand("a")
  }*/
}
