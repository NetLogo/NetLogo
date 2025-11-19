// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile

import org.nlogo.util.AnyFunSuiteEx

class LiteralsTests extends AnyFunSuiteEx {
  class C
  test("makeLiteralReporter not picky about type") {
    assertResult("_const:<C>")(
      Literals.makeLiteralReporter(new C).toString)
  }
}
