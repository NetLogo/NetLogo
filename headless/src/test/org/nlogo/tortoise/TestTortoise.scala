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
    val actual = evalJS(Compiler.compileReporter(logo))
    expectResult(expected)(actual)
  }

  def compareCommands(logo: String) {
    ws.clearOutput()
    ws.command(logo)
    val expected = ws.outputAreaBuffer.toString
    val (actual, json) =
      runJS(Compiler.compileCommands(logo, ws.procedures))
    // println(json) TODO: compare it to what comes from NetLogo
    expectResult(expected)(actual)
  }

  def defineProcedures(logo: String) {
    evalJS(Compiler.compileProcedures(logo))
    // setting the world size to something bigger than zero
    // so we can test things like:
    // cro 1 ask turtles [fd 1 outputprint ycor]
    ws.initForTesting(-5, 5, -5, 5, logo)
  }

  // these two are super helpful when running failing tests
  // the show the javascript before it gets executed.
  // TODO: what is the difference between eval and run?
  def evalJS(javascript: String) = {
    //println(javascript)
    Rhino.eval(javascript)
  }

  def runJS(javascript: String): (String, String) = {
    //println(javascript)
    Rhino.run(javascript)
  }

  def tester(testName: String)(body: => Unit) {
    test(testName) {
      ws = headless.HeadlessWorkspace.newInstance
      ws.silent = true
      // setting the world size to something bigger than zero
      // so we can test things like:
      // cro 1 ask turtles [fd 1 outputprint ycor]
      ws.initForTesting(5)
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
    compareCommands("clear-all")
    defineProcedures("""|to foo [x y z] cro x + y cro z end
                        |to goo [z] cro z * 10 end""".stripMargin)
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

  tester("rng") {
    compareCommands("random-seed 0 output-print random 100000")
  }

  tester("ask") {
    compareCommands("clear-all")
    compareCommands("cro 3")
    compareCommands("__ask-sorted turtles [ output-print 0 ]")
  }

  tester("turtle motion 1") {
    compareCommands("clear-all")
    compareCommands("cro 4 __ask-sorted turtles [fd 1] __ask-sorted turtles [output-print xcor output-print ycor]")
  }

  tester("turtle motion 2") {
    compareCommands("clear-all")
    compareCommands("cro 8 __ask-sorted turtles [fd 1] __ask-sorted turtles [output-print xcor output-print ycor]")
  }

  tester("turtle death") {
    compareCommands("clear-all")
    compareCommands("cro 8 __ask-sorted turtles [die]")
    compareCommands("__ask-sorted turtles [output-print xcor]")
  }

  tester("patches") {
    compareCommands("clear-all")
    compareCommands("__ask-sorted patches [output-print pxcor]")
  }

  tester("set globals") {
    compareCommands("clear-all")
    compareCommands("__ask-sorted patches [output-print pxcor]")
  }

  test("globals: set") {
    defineProcedures("globals [x] to foo [i] set x i output-print x end")
    compareCommands("clear-all")
    compareCommands("foo 5 foo 6 foo 7")
  }
  /*
  test("life") {
    val lifeSrc =
      """
        |patches-own [ living? live-neighbors ]
        |
        |to setup-random
        |  clear-all
        |  ask patches [ ifelse random-float 100.0 < 35 [ cell-birth ] [ cell-death ] ]
        |end
        |
        |to cell-birth set living? true  set pcolor white end
        |to cell-death set living? false set pcolor black end
        |
        |to go
        |  ask patches [ set live-neighbors count neighbors with [living?] ]
        |  ask patches [ ifelse live-neighbors = 3 [ cell-birth ] [ if live-neighbors != 2 [ cell-death ] ] ]
        |end
      """.stripMargin
    defineProcedures(lifeSrc)
    compareCommands("clear-all setup-random repeat 50 [go]")
    compareCommands("__ask-sorted patches [output-print living?]")
  }
  */
}
