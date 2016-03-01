// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.scalatest.FunSuite

class VectTests extends FunSuite {
  def v(x: Double, y: Double, z: Double) = new Vect(x, y, z)
  test("ToString") {
    assertResult("Vect(1.0,2.0,3.0)")(v(1, 2, 3).toString)
    assertResult("Vect(1.1,2.2,3.3)")(v(1.1, 2.2, 3.3).toString)
  }
  test("Equality") {
    assert(v(1, 2, 3) == v(1, 2, 3))
  }
  test("Magnitude") {
    assertResult(0)(v(0, 0, 0).magnitude)
    assertResult(1)(v(1, 0, 0).magnitude)
    assertResult(1)(v(0, 1, 0).magnitude)
    assertResult(1)(v(0, 0, 1).magnitude)
    assertResult(5)(v(0, 3, 4).magnitude)
    assertResult(9)(v(8, 4, 1).magnitude)
  }
  test("Inequality") {
    assert(v(1, 0, 0) != v(0, 0, 0))
    assert(v(0, 1, 0) != v(0, 0, 0))
    assert(v(0, 0, 1) != v(0, 0, 0))
    assert(v(0, 0, 0) != v(1, 0, 0))
    assert(v(0, 0, 0) != v(0, 1, 0))
    assert(v(0, 0, 0) != v(0, 0, 1))
  }
  test("Normalize0") {
    assertResult(v(0, 0, 0))(
      v(0, 0, 0).normalize)
  }
  test("Normalize1") {
    assertResult(v(1, 0, 0))(
      v(2, 0, 0).normalize)
  }
  test("Normalize2") {
    assertResult(v(0, 1, 0))(
      v(0, 2, 0).normalize)
  }
  test("Normalize3") {
    assertResult(v(0, 0, 1))(
      v(0, 0, 2).normalize)
  }
  test("Correct") {
    assertResult(v(0.1, 0.1, 0.1))(
      v(0.1, 0.1, 0.1).correct)
    assertResult(v(0, 0, 0))(
      v(1e-16, 1e-16, 1e-16).correct)
  }
  test("Invert") {
    assertResult(v(-1, -2, -3))(
      v(1, 2, 3).invert)
  }
  test("RotateX") {
    assertResult(v(0, -1, 0))(
      v(0, 0, 1).rotateX(90))
    assertResult(v(0, 1, 0))(
      v(0, 0, 1).rotateX(-90))
    // maybe someday we'll make this come out to -1.0 - ST 3/19/08
    assertResult(v(1, -0.9999999999999999, 1))(
      v(1, 1, 1).rotateX(90))
  }
}
