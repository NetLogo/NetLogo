// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile

import org.scalatest.funsuite.AnyFunSuite

class LiteralsTests extends AnyFunSuite {
  class C
  test("makeLiteralReporter not picky about type") {
    assertResult("_const:<C>")(
      Literals.makeLiteralReporter(new C).toString)
  }
}
