// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.scalatest.funsuite.AnyFunSuite
import org.nlogo.util.SlowTestTag

class SlowColorizerTests extends AnyFunSuite  {

  // very long Code tabs shouldn't blow the stack.
  // slow, hence SlowTest
  test("don't blow stack", SlowTestTag) {
    val longCode = io.Source.fromFile("models/test/Really Long Code.nls").mkString
    assertResult(1042326)(
      Colorizer.toHtml(longCode).size)
  }
}
