// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.ColorizerTheme
import org.nlogo.util.SlowTest

import org.scalatest.funsuite.AnyFunSuite

class SlowColorizerTests extends AnyFunSuite  {

  // very long Code tabs shouldn't blow the stack.
  // slow, hence SlowTest
  test("don't blow stack", SlowTest.Tag) {
    val longCode = io.Source.fromFile("models/test/Really Long Code.nls").mkString
    assertResult(1042326)(
      Colorizer.toHtml(longCode, ColorizerTheme.Light).size)
  }
}
