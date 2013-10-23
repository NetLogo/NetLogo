// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

class TestProcedures extends DockingSuite {

  test("procedure call 1") { implicit fixture => import fixture._
    declare("globals [g] to foo set g g + 2 end")
    testCommand("foo")
    testCommand("output-print g")
  }

  test("procedure call 2") { implicit fixture => import fixture._
    declare("globals [g] to foo set g g + 1 end")
    testCommand("foo foo foo")
    testCommand("output-print g")
  }

  test("procedure call with one input") { implicit fixture => import fixture._
    declare("globals [g] to foo [x] set g g + x end")
    testCommand("foo 1 foo 2 foo 3")
    testCommand("output-print g")
  }

  test("procedure call with three inputs") { implicit fixture => import fixture._
    declare("globals [g] to foo [x y z] set g g + x + y set g g + z end")
    testCommand("foo 1 2 3")
    testCommand("output-print g")
  }

  test("multiple procedures") { implicit fixture => import fixture._
    declare("""|globals [g]
               |to foo [x y z] set g g + x + y set g g + z end
               |to goo [z] set g g + z * 10 end""".stripMargin)
    testCommand("foo 1 2 3")
    testCommand("goo 2")
    testCommand("output-print g")
  }

  test("reporter procedure") { implicit fixture => import fixture._
    declare("to-report foo report 99 end")
    compare("foo")
  }

  test("recursive reporter procedure") { implicit fixture => import fixture._
    declare("""|to-report fact [n]
               |  ifelse n = 0
               |    [ report 1 ]
               |    [ report n * fact (n - 1) ]
               |end
               |""".stripMargin)
    compare("fact 6")
  }

}
