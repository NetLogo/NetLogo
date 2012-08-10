// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.scalatest.FunSuite

class TopologyTests extends FunSuite {

  def wrap(pos: Double, min: Double, max: Double): Double =
    Topology.wrap(pos, min, max)
      .ensuring(result => result >= min && result < max)

  test("wrap1") {
    // in the middle of range, leave input alone
    expect(0.0)(wrap(0, -1, 1))
    // at bottom of range, leave input alone
    expect(0.0)(wrap(0, 0, 1))
    // at top of range, wrap to bottom
    expect(0.0)(wrap(1, 0, 1))
  }

  // ticket #1038
  test("wrap2") {
    expect(-0.5)(wrap(-0.5000000000000001, -0.5, 1.5))
  }

}
