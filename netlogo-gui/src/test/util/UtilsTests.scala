// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util

import org.scalatest.{ FunSuite, PropSpec }
import org.scalatest.prop.PropertyChecks

// These are actually tests for the Utils class.
// Not to be confused with utilities for tests (TestUtils)
class UtilsTests extends FunSuite {
  test("getStackTrace") {
    val expected = "java.lang.Throwable\n" +
      " at org.nlogo.util.UtilsTests.$anonfun$new$1(UtilsTests.scala:"
    assert(Utils.getStackTrace(new Throwable).filter(_!='\r').take(expected.size) === expected)
  }
}

class UtilsTests2 extends PropSpec with PropertyChecks {

  import org.scalacheck.Gen
  import org.scalacheck.Arbitrary.arbitrary

  property("unescape is inverse of escape") {
    forAll((ns: String) =>
      assertResult(ns)(
        Utils.unescapeSpacesInURL(Utils.escapeSpacesInURL(ns))))}

  property("escape is inverse of unescape") {
    forAll((ns: String) =>
      assertResult(ns)(
        Utils.unescapeSpacesInURL(Utils.escapeSpacesInURL(ns))))}
}
