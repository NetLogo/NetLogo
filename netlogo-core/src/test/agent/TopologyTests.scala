// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.scalatest.FunSuite
import Topology.wrapPcor

class TopologyTests extends FunSuite {

  def wrap(pos: Double, min: Double, max: Double): Double =
    Topology.wrap(pos, min, max)
      .ensuring(result => result >= min && result < max)

  test("wrap1") {
    // in the middle of range, leave input alone
    assertResult(0.0)(wrap(0, -1, 1))
    // at bottom of range, leave input alone
    assertResult(0.0)(wrap(0, 0, 1))
    // at top of range, wrap to bottom
    assertResult(0.0)(wrap(1, 0, 1))
  }

  // ticket #1038
  test("wrap2") {
    assertResult(-0.5)(wrap(-0.5000000000000001, -0.5, 1.5))
  }

  test("wrapPcorPositive") {
    assertResult(0)(wrapPcor(0, 0, 1))
    assertResult(1)(wrapPcor(1, 0, 1))
    assertResult(0)(wrapPcor(2, 0, 1))
    assertResult(1)(wrapPcor(3, 0, 1))
    assertResult(0)(wrapPcor(0, -1, 0))
    assertResult(-1)(wrapPcor(1, -1, 0))
    assertResult(0)(wrapPcor(0, -1, 1))
    assertResult(1)(wrapPcor(1, -1, 1))
    assertResult(-1)(wrapPcor(2, -1, 1))
    assertResult(1)(wrapPcor(-2, -1, 1))
    assertResult(0)(wrapPcor(-3, -1, 1))
    assertResult(0)(wrapPcor(3, -1, 1))
    assertResult(-5)(wrapPcor(6, -5, 5))
    assertResult(5)(wrapPcor(-6, -5, 5))
  }

}
