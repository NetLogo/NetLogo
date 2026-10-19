// (C) Uri Wilensky. https://github.com/NetLogo/NW-Extension

package org.nlogo.extensions.nw

import org.nlogo.headless.HeadlessWorkspace
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.GivenWhenThen

class SetContextTestSuite extends AnyFunSuite with GivenWhenThen {
  test("avoid dangling links in context") {
    val ws: HeadlessWorkspace = HeadlessWorkspace.newInstance
    ws.setModelPath(new java.io.File("tests.txt").getPath)
    try {

      Given("two mice linked together and a frog linked to one mouse")
      ws.initForTesting(1, "extensions [nw]\n" + HeadlessWorkspace.TestDeclarations)
      ws.command("create-mice 2")
      ws.command("create-frogs 1")
      ws.command("ask one-of mice [ create-links-with other turtles ]")
      When("we set the context to `mice links`")
      ws.command("nw:set-context mice links")

      val gc = networkExtension(ws).getGraphContext(ws.world)

      Then("the context should contain the two mice")
      assertResult(2)(gc.nodes.size)
      And("only the one link between them")
      assertResult(1)(gc.links.size)

      And("the set of allEdges from all the turtles should be the same as `links`")
      assertResult(gc.nodes.flatMap(gc.allEdges(_)).toSeq)(gc.links.toSeq)

    } finally ws.dispose()
  }
}
