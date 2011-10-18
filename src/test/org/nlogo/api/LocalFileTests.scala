// (C) 2011 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.scalatest.FunSuite

class LocalFileTests extends FunSuite {
  test("file path") {
    new LocalFile("models/Sample Models/Earth Science/Fire.nlogo").readFile()
  }
}
