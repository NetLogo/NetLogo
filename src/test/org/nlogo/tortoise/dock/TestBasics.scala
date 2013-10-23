// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

class TestBasics extends DockingSuite {
  test("comments") { implicit fixture => import fixture._
    compare("3 ; comment")
    compare("[1 ; comment\n2]")
  }

  test("simple literals") { implicit fixture => import fixture._
    compare("false")
    compare("true")
    compare("2")
    compare("2.0")
    compare("\"foo\"")
  }

  test("literal lists") { implicit fixture => import fixture._
    compare("[]")
    compare("[1]")
    compare("[1 2]")
    compare("[\"foo\"]")
    compare("[1 \"foo\" 2]")
    compare("[1 [2] [3 4 [5]] 6 [] 7]")
    compare("[false true]")
  }

  test("arithmetic") { implicit fixture => import fixture._
    compare("2 + 2")
    compare("1 + 2 + 3")
    compare("1 - 2 - 3")
    compare("1 - (2 - 3)")
    compare("(1 - 2) - 3")
    compare("2 * 3 + 4 * 5")
    compare("6 / 2 + 12 / 6")
  }

  test("equality") { implicit fixture => import fixture._
    compare("5 = 5")
    compare(""""hello" = "hello"""")
  }

  test("empty commands") { implicit fixture => import fixture._
    testCommand("")
  }

  test("rng") { implicit fixture => import fixture._
    testCommand("output-print random 100000")
  }

  test("rng 2") { implicit fixture => import fixture._
    testCommand("output-print random-float 2.0")
  }

  test("printing") { implicit fixture => import fixture._
    testCommand("output-print 1")
    testCommand("output-print \"foo\"")
    testCommand("output-print 2 + 2")
    testCommand("output-print 1 output-print 2 output-print 3")
  }

  test("let") { implicit fixture => import fixture._
    testCommand("let x 5  output-print x")
  }

  test("globals: set") { implicit fixture => import fixture._
    declare("globals [x] to foo [i] set x i output-print x end")
    testCommand("foo 5 foo 6 foo 7")
  }

  test("clear-all clears globals") { implicit fixture => import fixture._
    declare("globals [g1 g2]")
    testCommand("set g1 88 set g2 99")
    testCommand("output-print (word g1 g2)")
    testCommand("clear-all")
    testCommand("output-print (word g1 g2)")
  }

  test("ticks basic") { implicit fixture => import fixture._
    testCommand("reset-ticks")
    compare("ticks")
    testCommand("tick")
    compare("ticks")
    testCommand("tick-advance 0.1")
    compare("ticks")
    testCommand("tick")
    compare("ticks")
    testCommand("reset-ticks")
    compare("ticks")
  }

  test("ticks non-negative") { implicit fixture => import fixture._
    testCommand("reset-ticks")
    testCommand("tick-advance -1")
    testCommand("tick")
    testCommand("tick-advance -0.1")
  }

  test("ticks need reset") { implicit fixture => import fixture._
    compare("ticks")
    testCommand("tick")
    testCommand("tick-advance 0.1")
    testCommand("reset-ticks")
    compare("ticks")
  }

  test("ticks clear") { implicit fixture => import fixture._
    testCommand("reset-ticks")
    compare("ticks")
    testCommand("clear-ticks")
    compare("ticks")
  }

  test("ticks clear all") { implicit fixture => import fixture._
    testCommand("reset-ticks")
    compare("ticks")
    testCommand("clear-all")
    compare("ticks")
  }

  test("timer") { implicit fixture => import fixture._
    compare("floor timer")
    testCommand("reset-timer")
    compare("floor timer")
    testCommand("clear-all")
    compare("floor timer")
  }

  test("resize world") { implicit fixture => import fixture._
    testCommand("resize-world -2 2 -2 2")
    testCommand("resize-world -5 5 -5 5")
    testCommand("resize-world -20 20 -20 20")
    testCommand("resize-world -25 25 -25 25")
    testCommand("resize-world 25 -25 25 -25")
    testCommand("resize-world 1 3 1 3")
    testCommand("resize-world 0 2 0 -2")
  }

  test("nobody") { implicit fixture => import fixture._
    testCommand("output-print nobody")
    testCommand("""if one-of turtles = nobody [ output-print "TEST" ]""")
    testCommand("cro 5")
    testCommand("""if one-of turtles = nobody [ output-print "TEST" ]""")
  }
}
