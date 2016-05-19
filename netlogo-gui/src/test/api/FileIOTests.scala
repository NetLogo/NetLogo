// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.scalatest.{ FunSuite, PropSpec }

import org.scalatest.prop.PropertyChecks

class FileIOTests extends FunSuite {
  test("getResourceLines") {
    val expected = "\nNetLogo author: Uri Wilensky"
    assert(FileIO.getResourceAsString("/system/about.txt").take(expected.size) ===
      expected)
  }
}

class FileIOTests2 extends PropSpec with PropertyChecks {

  import org.scalacheck.Gen
  import org.scalacheck.Arbitrary.arbitrary

  property("reader2String is inverse of StringReader") {
    forAll(arbitrary[String], Gen.chooseNum(1, 4096))(
      (ns, bufferSize) =>
        assertResult(ns)(
          FileIO.reader2String(new java.io.StringReader(ns), bufferSize)))}

}
