package org.nlogo.api

import org.scalatest.FunSuite

class LocalFileTests extends FunSuite {
  test("file path") {
    new LocalFile("models/Sample Models/Earth Science/Fire.nlogo").readFile()
  }
}
