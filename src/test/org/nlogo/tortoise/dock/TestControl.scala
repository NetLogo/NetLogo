// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

class TestControl extends DockingSuite {

  tester("while loops") {
    defineProcedures("")
    compareCommands("while [count turtles < 5] [cro 1]")
    compareCommands("output-print count turtles")
  }

  tester("let + while") {
    defineProcedures("")
    compareCommands(
      "let x 10 " +
      "while [x > 0] [ set x x - 1 ] " +
      "output-print x")
  }

  tester("if") {
    defineProcedures("")
    compareCommands("if true [ output-print 5 ]")
    compareCommands("if false [ output-print 5 ]")
  }

}
