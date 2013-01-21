// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise

import org.scalatest.FunSuite
import
  org.nlogo.{ api, nvm, headless, prim },
  org.nlogo.util.{ Femto, MersenneTwisterFast }

class TestTortoise extends FunSuite {

  var ws: headless.HeadlessWorkspace = null
  val owner = new api.SimpleJobOwner("Tortoise", new MersenneTwisterFast)

  def compare(logo: String) {
    val expected = ws.report(logo)
    val actual = Rhino.eval(Compiler.compileReporter(logo))
    expectResult(expected)(actual)
  }

  def compareCommands(logo: String) {
    ws.clearOutput()
    ws.command(logo)
    val expected = ws.outputAreaBuffer.toString
    val actual = Rhino.run(Compiler.compileCommands(logo, ws.procedures))
    expectResult(expected)(actual)
  }

  def defineProcedures(logo: String) {
    Rhino.eval(Compiler.compileProcedures(logo))
    ws.initForTesting(0, 0, 0, 0, logo)
  }

  def tester(testName: String)(body: => Unit) {
    test(testName) {
      ws = headless.HeadlessWorkspace.newInstance
      ws.silent = true
      ws.initForTesting(0)
      body
    }
  }

  ///

  tester("comments") {
    compare("3 ; comment")
    compare("[1 ; comment\n2]")
  }

  tester("simple literals") {
    compare("false")
    compare("true")
    compare("2")
    compare("2.0")
    compare("\"foo\"")
  }

  tester("literal lists") {
    compare("[]")
    compare("[1]")
    compare("[1 2]")
    compare("[\"foo\"]")
    compare("[1 \"foo\" 2]")
    compare("[1 [2] [3 4 [5]] 6 [] 7]")
    compare("[false true]")
  }

  tester("arithmetic") {
    compare("2 + 2")
    compare("1 + 2 + 3")
    compare("1 - 2 - 3")
    compare("1 - (2 - 3)")
    compare("(1 - 2) - 3")
    compare("2 * 3 + 4 * 5")
    compare("6 / 2 + 12 / 6")
  }

  tester("empty commands") {
    compareCommands("")
  }

  tester("printing") {
    compareCommands("output-print 1")
    compareCommands("output-print \"foo\"")
    compareCommands("output-print 2 + 2")
    compareCommands("output-print 1 output-print 2 output-print 3")
  }

  tester("agents") {
    compareCommands("clear-all")
    compareCommands("output-print count turtles")
    compareCommands("cro 5")
    compareCommands("output-print count turtles")
    compareCommands("clear-all")
    compareCommands("output-print count turtles")
  }

  tester("while loops") {
    compareCommands("clear-all")
    compareCommands("while [count turtles < 5] [cro 1]")
    compareCommands("output-print count turtles")
  }

  tester("let") {
    compareCommands("let x 5  output-print x")
  }

  tester("let + while") {
    compareCommands(
      "let x 10 " +
      "while [x > 0] [ set x x - 1 ] " +
      "output-print x")
  }

  tester("procedure call") {
    defineProcedures("to foo cro 1 end")
    compareCommands("clear-all")
    compareCommands("foo foo foo")
    compareCommands("output-print count turtles")
  }

  tester("procedure call with one input") {
    defineProcedures("to foo [x] cro x end")
    compareCommands("clear-all")
    compareCommands("foo 1 foo 2 foo 3")
    compareCommands("output-print count turtles")
  }

  tester("procedure call with three inputs") {
    defineProcedures("to foo [x y z] cro x + y cro z end")
    compareCommands("clear-all")
    compareCommands("foo 1 2 3")
    compareCommands("output-print count turtles")
  }

  tester("multiple procedures") {
    defineProcedures("""|to foo [x y z] cro x + y cro z end
                        |to goo [z] cro z * 10 end""".stripMargin)
    compareCommands("clear-all")
    compareCommands("foo 1 2 3")
    compareCommands("goo 10")
    compareCommands("output-print count turtles")
  }

  tester("if") {
    compareCommands("if true [ output-print 5 ]")
    compareCommands("if false [ output-print 5 ]")
  }

  tester("simple recursive call") {
    defineProcedures("to-report fact [n] ifelse n = 0 [ report 1 ] [ report n * fact (n - 1) ] end")
    compareCommands("output-print fact 6")
  }

}
