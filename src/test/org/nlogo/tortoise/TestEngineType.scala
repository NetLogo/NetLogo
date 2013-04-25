// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise

import org.scalatest.FunSuite

class TestEngineType extends FunSuite {

  test("engine name") {
    assert(Rhino.engine.getFactory.getEngineName === "Mozilla Rhino")
  }

}
