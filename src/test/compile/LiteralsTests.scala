// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile

import org.scalatest.FunSuite

class LiteralsTests extends FunSuite {
  test("makeLiteralReporter not picky about type") {
    case object Obj
    assertResult("_const:Obj")(
      Literals.makeLiteralReporter(Obj).toString)
  }
}
