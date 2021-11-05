// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import org.scalatest.funsuite.AnyFunSuite

class LogoListTests extends AnyFunSuite {
  test("iteratorNextThrowsException") {
    intercept[java.util.NoSuchElementException] {
      LogoList().javaIterator.next
    }
  }
}
