// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise

import org.scalatest.FunSuite
import
  org.nlogo.{ api, nvm, headless, mirror, prim },
  org.nlogo.util.{ Femto, MersenneTwisterFast }

class TestTortoise extends FunSuite {

  var ws: headless.HeadlessWorkspace = null
  val owner = new api.SimpleJobOwner("Tortoise", new MersenneTwisterFast)
  def mirrorables: Iterable[mirror.Mirrorable] =
    mirror.Mirrorables.allMirrorables(ws.world)
  var state: mirror.Mirroring.State = Map()

  def compare(logo: String) {
    val expected = ws.report(logo)
    val actual = evalJS(Compiler.compileReporter(logo))
    expectResult(expected)(actual)
  }

  def compareCommands(logo: String) {
    // println(s"logo = $logo")
    ws.clearOutput()
    ws.command(logo)
    val (newState, update) = mirror.Mirroring.diffs(state, mirrorables)
    state = newState
    // println(s"state = $state")
    // println(s"update = $update")
    val expectedJson = "[" + mirror.JSONSerializer.serialize(update) + "]"
    // println(s"expectedJson = $expectedJson")
    val expectedOutput = ws.outputAreaBuffer.toString
    val (actualOutput, actualJson) =
      runJS(Compiler.compileCommands(logo, ws.procedures, ws.world.program))
    expectResult(expectedOutput)(actualOutput)
    Rhino.eval("expectedModel = new AgentModel")
    Rhino.eval("actualModel = new AgentModel")
    Rhino.eval("expectedUpdates = " + expectedJson)
    Rhino.eval("actualUpdates = " + actualJson)
    Rhino.eval("expectedModel.updates(expectedUpdates)")
    Rhino.eval("actualModel.updates(actualUpdates)")
    val expectedModel = Rhino.eval("JSON.stringify(expectedModel)").asInstanceOf[String]
    val actualModel = Rhino.eval("JSON.stringify(actualModel)").asInstanceOf[String]
    // println(" exp upt = " + expectedJson)
    // println(" act upt = " + actualJson)
    // println("expected = " + expectedModel)
    // println("  actual = " + actualModel)
    org.skyscreamer.jsonassert.JSONAssert.assertEquals(
      expectedModel, actualModel, true)  // strict = true
    // println()
  }

  // use single-patch world by default to keep generated JSON to a minimum
  def defineProcedures(logo: String, minPxcor: Int = 0, maxPxcor: Int = 0, minPycor: Int = 0, maxPycor: Int = 0) {
    evalJS(Compiler.compileProcedures(logo, minPxcor, maxPxcor, minPycor, maxPycor))
    ws.initForTesting(minPxcor, maxPxcor, minPycor, maxPycor, logo)
    state = Map()
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
      defineProcedures("")
      state = Map()
      compareCommands("clear-all")
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

  tester("equality") {
    compare("5 = 5")
    compare(""""hello" = "hello"""")
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

  tester("turtle creation") {
    compareCommands("output-print count turtles")
    compareCommands("cro 1")
    compareCommands("output-print count turtles")
    compareCommands("cro 4")
    compareCommands("output-print count turtles")
    compareCommands("clear-all")
    compareCommands("output-print count turtles")
  }

  tester("while loops") {
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
    compareCommands("cro 3")
    compareCommands("__ask-sorted turtles [ output-print 0 ]")
  }

  tester("turtle motion 1") {
    defineProcedures("", -1, 1, -1, 1)
    compareCommands("cro 4 __ask-sorted turtles [fd 1] __ask-sorted turtles [output-print xcor output-print ycor]")
  }

  tester("turtle motion 2") {
    defineProcedures("", -1, 1, -1, 1)
    compareCommands("cro 8 __ask-sorted turtles [fd 1] __ask-sorted turtles [output-print xcor output-print ycor]")
  }

  tester("turtle death") {
    compareCommands("cro 8 __ask-sorted turtles [die]")
    compareCommands("__ask-sorted turtles [output-print xcor]")
  }

  tester("turtle size") {
    compareCommands("cro 1 __ask-sorted turtles [ set size 5 ]")
    compareCommands("__ask-sorted turtles [ output-print size ]")
  }

  tester("turtle color") {
    compareCommands("cro 1 __ask-sorted turtles [ set color blue ]")
    compareCommands("__ask-sorted turtles [ output-print blue ]")
  }

  tester("patches") {
    compareCommands("__ask-sorted patches [output-print pxcor]")
  }

  tester("globals: set") {
    defineProcedures("globals [x] to foo [i] set x i output-print x end")
    compareCommands("foo 5 foo 6 foo 7")
  }

  tester("patch variables") {
    val src =
      """
        |patches-own [ living? live-neighbors ]
        |to cellbirth set living? true  set pcolor white end
        |to celldeath set living? false set pcolor black end
      """.stripMargin
    defineProcedures(src)
    compareCommands("__ask-sorted patches [cellbirth output-print living?]")
    compareCommands("__ask-sorted patches [celldeath output-print living?]")
  }

  tester("patch order"){
    defineProcedures("", -5, 5, -5, 5)
    compareCommands("""__ask-sorted patches [output-print (word pxcor "," pycor)]""")
  }

  tester("turtles get patch variables"){
    defineProcedures("", -5, 5, -5, 5)
    compareCommands("cro 5 __ask-sorted turtles [ fd 1 ]")
    compareCommands("""__ask-sorted turtles [output-print (word pxcor "," pycor)]""")
  }

  tester("turtles set patch variables"){
    defineProcedures("", -5, 5, -5, 5)
    compareCommands("cro 5 __ask-sorted turtles [ fd 1 set pcolor blue ]")
    compareCommands("__ask-sorted turtles [output-print color]")
    compareCommands("__ask-sorted turtles [output-print pcolor]")
    compareCommands("__ask-sorted patches [output-print pcolor]")
  }

  tester("with"){
    defineProcedures("", -5, 5, -5, 5)
    compareCommands("__ask-sorted patches with [pxcor = 1] [output-print pycor]")
  }

  tester("with 2"){
    defineProcedures("", -5, 5, -5, 5)
    compareCommands("__ask-sorted patches with [pxcor = -3 and pycor = 2] [ output-print self ]")
  }

  tester("with + turtles accessing turtle and patch vars"){
    defineProcedures("", -5, 5, -5, 5)
    compareCommands("cro 5 ask turtles [fd 1]")
    compareCommands("__ask-sorted turtles with [pxcor =  1] [output-print pycor]")
    compareCommands("__ask-sorted turtles with [pxcor = -1] [output-print ycor]")
  }

  tester("get patch") {
    compareCommands("output-print patch 0 0")
  }

  tester("get turtle") {
    compareCommands("cro 5")
    compareCommands("__ask-sorted turtles [ output-print self ]")
  }

  tester("patch set") {
    defineProcedures("", -5, 5, -5, 5)
    compareCommands("__ask-sorted patches with [pxcor = -1 and pycor = 0] [ set pcolor green ]")
    compareCommands("ask patch 0 0 [ set pcolor green ]")
    compareCommands("output-print count patches with [pcolor = green]")
  }

  tester("and, or") {
    defineProcedures("", -5, 5, -5, 5)
    compareCommands("output-print count patches with [pxcor = 0 or pycor = 0]")
    compareCommands("output-print count patches with [pxcor = 0 and pycor = 0]")
  }

//  tester("neighbors") {
//    defineProcedures("", -5, 5, -5, 5)
//    compareCommands("""__ask-sorted patches [ __ask-sorted neighbors [ output-print (word pxcor ", " pycor) ]]""")
//  }

  tester("setting a built-in patch variable") {
    defineProcedures("", -5, 5, -5, 5)
    compareCommands("__ask-sorted patches with [pxcor = 2 and pycor = 3] [ set pcolor green ]")
    compareCommands("output-print count patches with [pcolor = green]")
    compareCommands("__ask-sorted patches [ output-print self output-print pcolor ]")
  }

  tester("setting a patches-own variable") {
    defineProcedures("patches-own [foo]", -5, 5, -5, 5)
    compareCommands("__ask-sorted patches with [pxcor = 2 and pycor = 3] [ set foo green ]")
    compareCommands("output-print count patches with [foo = green]")
    compareCommands("__ask-sorted patches [ output-print self output-print foo ]")
  }

  /*
  tester("life") {
    val lifeSrc =
      """
        |patches-own [ living? live-neighbors ]
        |
        |to setup
        |  clear-all
        |  ask patches [set living? false]
        |  ask patch  0  0 [ set living? true ]
        |  ask patch -1  0 [ set living? true ]
        |  ask patch  0 -1 [ set living? true ]
        |  ask patch  0  1 [ set living? true ]
        |  ask patch  1  1 [ set living? true ]
        |end
        |
        |to cellbirth set living? true  set pcolor white end
        |to celldeath set living? false set pcolor 0.0 end
        |
        |to go
        |  ask patches [
        |    set live-neighbors count neighbors with [living?] ]
        |  ask patches [ ifelse live-neighbors = 3 [ cellbirth ] [ if live-neighbors != 2 [ celldeath ] ] ]
        |end
      """.stripMargin
    defineProcedures(lifeSrc, -5, 5, -5, 5)
    compareCommands("setup")
    for (_ <- 1 to 5)
      compareCommands("go")
    compareCommands("""__ask-sorted patches [output-print (word "(" pxcor ", " pycor ") -> " living?) ]""")
  }
  */

}
