// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

class TestControl extends DockingSuite {

  test("while loops") { implicit fixture => import fixture._
    testCommand("while [count turtles < 5] [cro 1]")
    testCommand("output-print count turtles")
  }

  test("let + while") { implicit fixture => import fixture._
    testCommand(
      "let x 10 " +
      "while [x > 0] [ set x x - 1 ] " +
      "output-print x")
  }

  test("if") { implicit fixture => import fixture._
    testCommand("if true [ output-print 5 ]")
    testCommand("if false [ output-print 5 ]")
  }

}
