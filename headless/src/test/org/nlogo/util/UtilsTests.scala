// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util

import org.scalatest.{ FunSuite, PropSpec }
import org.scalatest.prop.PropertyChecks

// These are actually tests for the Utils class.
// Not to be confused with utilities for tests (TestUtils)
class UtilsTests extends FunSuite {
  test("getStackTrace") {
    val expected = "java.lang.Throwable\n" +
      " at org.nlogo.util.UtilsTests$$anonfun$1.apply$mcV$sp(UtilsTests.scala:"
    assert(Utils.getStackTrace(new Throwable).filter(_!='\r').take(expected.size) === expected)
  }
  test("getResourceLines") {
    val expected = "NetLogo author: Uri Wilensky\n"
    assert(Utils.getResourceAsString("/system/about.txt").take(expected.size) ===
      expected)
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

  property("reader2String is inverse of StringReader") {
    forAll(arbitrary[String], Gen.chooseNum(1, 4096))(
      (ns, bufferSize) =>
        assertResult(ns)(
          Utils.reader2String(new java.io.StringReader(ns), bufferSize)))}

}

class UniqueNameUtilTests extends FunSuite {
  test("uniqueName") {
    assert(Utils.uniqueName("foo", Seq.empty) == "foo")
    assert(Utils.uniqueName("foo", Seq("foo")) == "foo (1)")
    assert(Utils.uniqueName("foo (1)", Seq("foo (1)")) == "foo (2)")
    assert(Utils.uniqueName("foo (1)", Seq("foo(1)")) == "foo (2)")
    assert(Utils.uniqueName("foo(1)", Seq("foo (1)")) == "foo (2)")
    assert(Utils.uniqueName("foo(1)", Seq("foo(1)")) == "foo (2)")
    assert(Utils.uniqueName("foo", Seq("foo", "foo (1)")) == "foo (2)")
    assert(Utils.uniqueName("foo", Seq("foo (1)", "foo (2)")) == "foo (3)")
    assert(Utils.uniqueName("foo", Seq("foo", "foo (2)")) == "foo (3)")
    assert(Utils.uniqueName("foo (99)", Seq("foo (99)")) == "foo (100)")
    assert(Utils.uniqueName("foo (100)", Seq("foo (1)")) == "foo (100)")
    assert(Utils.uniqueName("foo (100)", Seq("foo (100)")) == "foo (101)")
    assert(Utils.uniqueName("foo (100)", Seq("foo (baz)")) == "foo (100)")
    assert(Utils.uniqueName("foo (bar)", Seq("foo (baz)")) == "foo (bar)")
    assert(Utils.uniqueName("foo (bar)", Seq("foo (100)")) == "foo (bar)")
  }
}
