// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

class TestProcedures extends DockingSuite {

  test("procedure call") { implicit fixture => import fixture._
    declare("to foo cro 1 end")
    testCommand("foo foo foo")
    testCommand("output-print count turtles")
  }

  test("procedure call with one input") { implicit fixture => import fixture._
    declare("to foo [x] cro x end")
    testCommand("foo 1 foo 2 foo 3")
    testCommand("output-print count turtles")
  }

  test("procedure call with three inputs") { implicit fixture => import fixture._
    declare("to foo [x y z] cro x + y cro z end")
    testCommand("foo 1 2 3")
    testCommand("output-print count turtles")
  }

  test("multiple procedures") { implicit fixture => import fixture._
    declare("""|to foo [x y z] cro x + y cro z end
                        |to goo [z] cro z * 10 end""".stripMargin)
    testCommand("foo 1 2 3")
    testCommand("goo 2")
    testCommand("output-print count turtles")
  }

  test("simple recursive call") { implicit fixture => import fixture._
    declare("to-report fact [n] ifelse n = 0 [ report 1 ] [ report n * fact (n - 1) ] end")
    testCommand("output-print fact 6")
  }

}
