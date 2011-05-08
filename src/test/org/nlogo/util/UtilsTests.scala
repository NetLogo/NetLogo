package org.nlogo.util

import org.scalatest.FunSuite

import org.scalacheck.{Properties, Prop}
import Prop._

// These are actually tests for the Utils class.
// Not to be confused with utilities for tests (TestUtils)
class UtilsTests extends FunSuite {
  test("getStackTrace") {
    val expected = "java.lang.Throwable\n" +
      " at org.nlogo.util.UtilsTests$$anonfun$1.apply$mcV$sp(UtilsTests.scala:"
    assert(Utils.getStackTrace(new Throwable).take(expected.size) === expected)
  }
  test("getResourceLines") {
    val expected = "NetLogo author: Uri Wilensky\n"
    assert(Utils.getResourceAsString("/system/about.txt").take(expected.size) ===
      expected)
  }
}

object UtilsTests2 extends Properties("Utils") {

  property("unescape is inverse of escape") =
    forAll((ns: String) =>
      ns == Utils.unescapeSpacesInURL(Utils.escapeSpacesInURL(ns)))

  property("escape is inverse of unescape") =
    forAll((ns: String) =>
      ns == Utils.unescapeSpacesInURL(Utils.escapeSpacesInURL(ns)))

  property("reader2String is inverse of StringReader") =
    forAll((ns: String, bufferSize: Int) =>
      (bufferSize > 0 && bufferSize <= 4096) ==>  // we don't need to test gigantic buffers - ST 12/22/09
        { ns == Utils.reader2String(new java.io.StringReader(ns), bufferSize) })

}
