// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util

import org.nlogo.util.AnyFunSuiteEx

import org.scalatest.propspec.AnyPropSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

// These are actually tests for the Utils class.
// Not to be confused with utilities for tests (TestUtils)
class UtilsTests extends AnyFunSuiteEx {
  test("getStackTrace") {
    val expected = "java.lang.Throwable\n" +
      " at org.nlogo.util.UtilsTests.testFun$proxy1$1(UtilsTests.scala:"
    assert(Utils.getStackTrace(new Throwable).filter(_!='\r').take(expected.size) === expected)
  }
}

class UtilsTests2 extends AnyPropSpec with ScalaCheckPropertyChecks {
  property("unescape is inverse of escape") {
    forAll((ns: String) =>
      assertResult(ns)(
        Utils.unescapeSpacesInURL(Utils.escapeSpacesInURL(ns))))}

  property("escape is inverse of unescape") {
    forAll((ns: String) =>
      assertResult(ns)(
        Utils.unescapeSpacesInURL(Utils.escapeSpacesInURL(ns))))}
}
