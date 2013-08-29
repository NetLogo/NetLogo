// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

class TestBasics extends DockingSuite {

  tester("comments") {
    defineProcedures("")
    compare("3 ; comment")
    compare("[1 ; comment\n2]")
  }

  tester("simple literals") {
    defineProcedures("")
    compare("false")
    compare("true")
    compare("2")
    compare("2.0")
    compare("\"foo\"")
  }

  tester("literal lists") {
    defineProcedures("")
    compare("[]")
    compare("[1]")
    compare("[1 2]")
    compare("[\"foo\"]")
    compare("[1 \"foo\" 2]")
    compare("[1 [2] [3 4 [5]] 6 [] 7]")
    compare("[false true]")
  }

  tester("arithmetic") {
    defineProcedures("")
    compare("2 + 2")
    compare("1 + 2 + 3")
    compare("1 - 2 - 3")
    compare("1 - (2 - 3)")
    compare("(1 - 2) - 3")
    compare("2 * 3 + 4 * 5")
    compare("6 / 2 + 12 / 6")
  }

  tester("equality") {
    defineProcedures("")
    compare("5 = 5")
    compare(""""hello" = "hello"""")
  }

  tester("empty commands") {
    defineProcedures("")
    compareCommands("")
  }

  tester("rng") {
    defineProcedures("")
    compareCommands("random-seed 0 output-print random 100000")
  }

  tester("printing") {
    defineProcedures("")
    compareCommands("output-print 1")
    compareCommands("output-print \"foo\"")
    compareCommands("output-print 2 + 2")
    compareCommands("output-print 1 output-print 2 output-print 3")
  }

  tester("let") {
    defineProcedures("")
    compareCommands("let x 5  output-print x")
  }

  tester("globals: set") {
    defineProcedures("globals [x] to foo [i] set x i output-print x end")
    compareCommands("foo 5 foo 6 foo 7")
  }

  tester("clear-all clears globals") {
    defineProcedures("globals [g1 g2]")
    compareCommands("set g1 88 set g2 99")
    compareCommands("output-print (word g1 g2)")
    compareCommands("clear-all")
    compareCommands("output-print (word g1 g2)")
  }

}
