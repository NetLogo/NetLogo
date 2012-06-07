// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.client

import org.scalatest.FunSuite

class TimedSetTests extends FunSuite {

  // note that this doesn't really test much of anything -- it just tests that you can add stuff, it
  // doesn't test that the adds are actually handled properly - ST 1/28/05
  test("TimedSet") {
    val ts = new TimedSet(500)
    ts.add("X")
    ts.add("X")
    ts.add("X")
    Thread.sleep(250)
    ts.add("Y")
    Thread.sleep(50)
    ts.add("Z")
    while (ts.size > 0) {
      ts.expire()
      ts.toArray
    }
  }

}
