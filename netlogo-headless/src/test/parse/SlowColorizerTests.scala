// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.ColorizerTheme
import org.nlogo.util.{ AnyFunSuiteEx, SlowTest }

import scala.io.Source

class SlowColorizerTests extends AnyFunSuiteEx  {

  // very long Code tabs shouldn't blow the stack.
  // slow, hence SlowTest
  test("don't blow stack", SlowTest.Tag) {
    val longCode = Source.fromFile("models/test/Really Long Code.nls").mkString
    assertResult(1042326)(
      Colorizer.toHtml(longCode, ColorizerTheme.Light).size)
  }
}
