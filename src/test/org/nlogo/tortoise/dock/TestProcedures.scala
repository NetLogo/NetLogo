// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

class TestProcedures extends DockingSuite {
  tester("procedure call") {
    defineProcedures("to foo cro 1 end")
    compareCommands("foo foo foo")
    compareCommands("output-print count turtles")
  }

  tester("procedure call with one input") {
    defineProcedures("to foo [x] cro x end")
    compareCommands("foo 1 foo 2 foo 3")
    compareCommands("output-print count turtles")
  }

  tester("procedure call with three inputs") {
    defineProcedures("to foo [x y z] cro x + y cro z end")
    compareCommands("foo 1 2 3")
    compareCommands("output-print count turtles")
  }

  tester("multiple procedures") {
    defineProcedures("""|to foo [x y z] cro x + y cro z end
                        |to goo [z] cro z * 10 end""".stripMargin)
    compareCommands("foo 1 2 3")
    compareCommands("goo 2")
    compareCommands("output-print count turtles")
  }

  tester("simple recursive call") {
    defineProcedures("to-report fact [n] ifelse n = 0 [ report 1 ] [ report n * fact (n - 1) ] end")
    compareCommands("output-print fact 6")
  }

}
