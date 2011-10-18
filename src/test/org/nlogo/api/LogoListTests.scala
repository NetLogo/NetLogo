// (C) 2011 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.scalatest.FunSuite

class LogoListTests extends FunSuite {
  test("iteratorNextThrowsException") {
    intercept[java.util.NoSuchElementException] {
      LogoList().iterator.next
    }
  }
}
