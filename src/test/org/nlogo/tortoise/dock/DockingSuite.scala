// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

import org.scalatest.FunSuite
import
  org.nlogo.{ api, headless, mirror },
  org.nlogo.util.MersenneTwisterFast

trait DockingSuite extends FunSuite {

  val rhino = new Rhino
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
    rhino.eval("expectedUpdates = " + expectedJson)
    rhino.eval("actualUpdates = " + actualJson)
    rhino.eval("expectedModel.updates(expectedUpdates)")
    rhino.eval("actualModel.updates(actualUpdates)")
    val expectedModel = rhino.eval("JSON.stringify(expectedModel)").asInstanceOf[String]
    val actualModel = rhino.eval("JSON.stringify(actualModel)").asInstanceOf[String]
    // println(" exp upt = " + expectedJson)
    // println(" act upt = " + actualJson)
    // println("expected = " + expectedModel)
    // println("  actual = " + actualModel)
    org.skyscreamer.jsonassert.JSONAssert.assertEquals(
      expectedModel, actualModel, true)  // strict = true
    // println()
  }

  // use single-patch world by default to keep generated JSON to a minimum
  def defineProcedures(logo: String, dimensions: api.WorldDimensions = api.WorldDimensions.square(0)) {
    val (js, _, _) = Compiler.compileProcedures(logo, dimensions)
    evalJS(js)
    headless.ModelCreator.open(ws, dimensions, logo)
    state = Map()
    rhino.eval("expectedModel = new AgentModel")
    rhino.eval("actualModel = new AgentModel")
    compareCommands("clear-all")
  }

  // these two are super helpful when running failing tests
  // the show the javascript before it gets executed.
  // TODO: what is the difference between eval and run?
  def evalJS(javascript: String) = {
    //println(javascript)
    rhino.eval(javascript)
  }

  def runJS(javascript: String): (String, String) = {
    //println(javascript)
    rhino.run(javascript)
  }

  def tester(testName: String)(body: => Unit) {
    test(testName) {
      ws = headless.HeadlessWorkspace.newInstance
      ws.silent = true
      state = Map()
      try body
      finally ws.dispose()
    }
  }

}
