// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.LogoList
import org.nlogo.util.AnyFunSuiteEx

class LogoListTests extends AnyFunSuiteEx {
  test("iteratorNextThrowsException") {
    intercept[java.util.NoSuchElementException] {
      LogoList().iterator.next()
    }
  }
}
