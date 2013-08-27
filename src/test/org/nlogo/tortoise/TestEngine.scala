// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise

import org.scalatest.FunSuite

// just basic smoke tests that basic Tortoise engine functionality is there,
// without involving the Tortoise compiler

class TestEngine extends FunSuite {

  test("can eval a number literal") {
    assertResult(Double.box(2)) {
      Rhino.eval("2.0")
    }
  }

  test("empty world") {
    Rhino.eval("world = new World(-1, 1, -1, 1)")
    Rhino.eval("world.clearall()")
    assertResult(Double.box(9)) {
      Rhino.eval("AgentSet.count(world.patches())")
    }
  }

}
