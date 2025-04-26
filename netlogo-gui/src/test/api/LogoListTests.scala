// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.LogoList
import org.scalatest.funsuite.AnyFunSuite

class LogoListTests extends AnyFunSuite {
  test("iteratorNextThrowsException") {
    intercept[java.util.NoSuchElementException] {
      LogoList().iterator.next()
    }
  }
}
