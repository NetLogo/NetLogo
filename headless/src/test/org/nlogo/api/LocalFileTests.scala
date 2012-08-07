// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.scalatest.FunSuite

class LocalFileTests extends FunSuite {
  test("file path") {
    new LocalFile("headless/resources/system/tokens.txt").readFile()
  }
}
