// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise

import org.scalatest.FunSuite
import
  org.nlogo.{ api, nvm, headless, mirror, prim },
  org.nlogo.util.{ Femto, MersenneTwisterFast }

class TestDocking extends FunSuite {

  var ws: headless.HeadlessWorkspace = null
  val owner = new api.SimpleJobOwner("Tortoise", new MersenneTwisterFast)
  def mirrorables: Iterable[mirror.Mirrorable] =
    mirror.Mirrorables.allMirrorables(ws.world, Seq())
  var state: mirror.Mirroring.State = Map()

  def compare(logo: String) {
    val expected = ws.report(logo)
    val actual = evalJS(Compiler.compileReporter(logo))
    assertResult(expected)(actual)
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
    assertResult(expectedOutput)(actualOutput)
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
    val (js, _, _) = Compiler.compileProcedures(logo, minPxcor, maxPxcor, minPycor, maxPycor)
    evalJS(js)
    headless.InitForTesting(ws, api.WorldDimensions(minPxcor, maxPxcor, minPycor, maxPycor), logo)
    state = Map()
    Rhino.eval("expectedModel = new AgentModel")
    Rhino.eval("actualModel = new AgentModel")
    compareCommands("clear-all")
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
      state = Map()
      body
    }
  }

  ///

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

  tester("word 0") {
    defineProcedures("")
    compare("(word)")
  }

  tester("word 1") {
    defineProcedures("")
    compare("(word 1)")
  }

  tester("word") {
    defineProcedures("")
    compare("(word 1 2 3)") // 123, and hopefully not, god forbid, 6
  }

  tester("empty commands") {
    defineProcedures("")
    compareCommands("")
  }

  tester("printing") {
    defineProcedures("")
    compareCommands("output-print 1")
    compareCommands("output-print \"foo\"")
    compareCommands("output-print 2 + 2")
    compareCommands("output-print 1 output-print 2 output-print 3")
  }

  tester("turtle creation") {
    defineProcedures("")
    compareCommands("output-print count turtles")
    compareCommands("cro 1")
    compareCommands("output-print count turtles")
    compareCommands("cro 4")
    compareCommands("output-print count turtles")
    compareCommands("clear-all")
    compareCommands("output-print count turtles")
  }

  tester("while loops") {
    defineProcedures("")
    compareCommands("while [count turtles < 5] [cro 1]")
    compareCommands("output-print count turtles")
  }

  tester("let") {
    defineProcedures("")
    compareCommands("let x 5  output-print x")
  }

  tester("let + while") {
    defineProcedures("")
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
    defineProcedures("")
    compareCommands("if true [ output-print 5 ]")
    compareCommands("if false [ output-print 5 ]")
  }

  tester("simple recursive call") {
    defineProcedures("to-report fact [n] ifelse n = 0 [ report 1 ] [ report n * fact (n - 1) ] end")
    compareCommands("output-print fact 6")
  }

  tester("rng") {
    defineProcedures("")
    compareCommands("random-seed 0 output-print random 100000")
  }

  tester("crt") {
    defineProcedures("")
    compareCommands("random-seed 0 crt 10")
    compareCommands("__ask-sorted turtles [ output-print color output-print heading ]")
  }

  tester("random-xcor/ycor") {
    defineProcedures("")
    compareCommands("cro 10")
    compareCommands("random-seed 0 __ask-sorted turtles [ setxy random-xcor random-ycor ]")
  }

  tester("ask") {
    defineProcedures("")
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
    defineProcedures("")
    compareCommands("cro 8")
    compareCommands("__ask-sorted turtles [die]")
    compareCommands("__ask-sorted turtles [output-print xcor]")
  }

  tester("turtle size") {
    defineProcedures("")
    compareCommands("cro 1 __ask-sorted turtles [ set size 5 ]")
    compareCommands("__ask-sorted turtles [ output-print size ]")
  }

  tester("turtle color") {
    defineProcedures("")
    compareCommands("cro 1 __ask-sorted turtles [ set color blue ]")
    compareCommands("__ask-sorted turtles [ output-print blue ]")
  }

  tester("patches") {
    defineProcedures("")
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
    compareCommands("""__ask-sorted patches [ output-print self ]""")
  }

  tester("turtles get patch variables"){
    defineProcedures("", -5, 5, -5, 5)
    compareCommands("cro 5 __ask-sorted turtles [ fd 1 ]")
    compareCommands("""__ask-sorted turtles [ output-print self ]""")
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
    defineProcedures("")
    compareCommands("output-print patch 0 0")
  }

  tester("get turtle") {
    defineProcedures("")
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
//    compareCommands("""__ask-sorted patches [ __ask-sorted neighbors [ output-print self ]]""")
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

  tester("clear-all clears globals") {
    defineProcedures("globals [g1 g2]")
    compareCommands("set g1 88 set g2 99")
    compareCommands("output-print (word g1 g2)")
    compareCommands("clear-all")
    compareCommands("output-print (word g1 g2)")
  }

  tester("clear-all clears patches") {
    defineProcedures("patches-own [p]")
    compareCommands("ask patches [ set p 123 ]")
    compareCommands("ask patches [ set pcolor green ]")
    compareCommands("clear-all")
    compareCommands("output-print count patches with [pcolor = green]")
  }

  tester("sprout") {
    defineProcedures("")
    compareCommands("random-seed 0 " +
      "__ask-sorted patches with [pxcor >= 0] [ sprout 1 ]")
  }

  tester("life") {
    val lifeSrc =
      """
        |patches-own [ living? live-neighbors ]
        |
        |to setup
        |  clear-all
        |  ask patches [ celldeath ]
        |  ask patch  0  0 [ cellbirth ]
        |  ask patch -1  0 [ cellbirth ]
        |  ask patch  0 -1 [ cellbirth ]
        |  ask patch  0  1 [ cellbirth ]
        |  ask patch  1  1 [ cellbirth ]
        |end
        |
        |to cellbirth set living? true  set pcolor white end
        |to celldeath set living? false set pcolor black end
        |
        |to go
        |  ask patches [
        |    set live-neighbors count neighbors with [living?] ]
        |  ask patches [
        |    ifelse live-neighbors = 3
        |      [ cellbirth ]
        |      [ if live-neighbors != 2
        |        [ celldeath ] ] ]
        |end
      """.stripMargin
    defineProcedures(lifeSrc, -5, 5, -5, 5)
    compareCommands("setup")
    for (_ <- 1 to 5)
      compareCommands("go")
    compareCommands("""__ask-sorted patches [output-print (word self " -> " living?) ]""")
  }

  tester("turtle motion") {
    defineProcedures("", -5, 5, -5, 5)
    compareCommands("random-seed 0 crt 100")
    compareCommands("__ask-sorted turtles [ setxy random-xcor random-ycor ]")
    for (_ <- 1 to 10)
      compareCommands("__ask-sorted turtles [ fd 1 ]")
  }

  tester("termites") {
    val code =
      """
       |turtles-own [next steps]
       |
       |to setup
       |  clear-all
       |  __ask-sorted patches [
       |    if random 100 < 20
       |      [ set pcolor yellow ] ]
       |  crt 50
       |  __ask-sorted turtles [
       |    set color white
       |    setxy random-xcor random-ycor
       |    set size 3
       |    set next 1
       |  ]
       |end
       |
       |to go
       |  __ask-sorted turtles
       |    [ ifelse steps > 0
       |        [ set steps steps - 1 ]
       |        [ action
       |          wiggle ]
       |      fd 1 ]
       |end
       |
       |to wiggle
       |  rt random 50
       |  lt random 50
       |end
       |
       |to action
       |  ifelse next = 1
       |    [ searchforchip ]
       |    [ ifelse next = 2
       |      [ findnewpile ]
       |      [ ifelse next = 3
       |        [ putdownchip ]
       |        [ getaway ] ] ]
       |end
       |
       |to searchforchip
       |  if pcolor = yellow
       |    [ set pcolor black
       |      set color orange
       |      set steps 20
       |      set next 2 ]
       |end
       |
       |to findnewpile
       |  if pcolor = yellow
       |    [ set next 3 ]
       |end
       |
       |to putdownchip
       |  if pcolor = black
       |   [ set pcolor yellow
       |     set color white
       |     set steps 20
       |     set next 4 ]
       |end
       |
       |to getaway
       |  if pcolor = black
       |    [ set next 1 ]
       |end
      """.stripMargin
    defineProcedures(code, -20, 20, -20, 20)
    compareCommands("random-seed 0 setup")
    for (_ <- 1 to 20)
      compareCommands("go")
  }

}
