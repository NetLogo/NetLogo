// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.scalatest.FunSuite

class VectTests extends FunSuite {
  def v(x: Double, y: Double, z: Double) = new Vect(x, y, z)
  test("ToString") {
    expectResult("Vect(1.0,2.0,3.0)")(v(1, 2, 3).toString)
    expectResult("Vect(1.1,2.2,3.3)")(v(1.1, 2.2, 3.3).toString)
  }
  test("Equality") {
    assert(v(1, 2, 3) == v(1, 2, 3))
  }
  test("Magnitude") {
    expectResult(0)(v(0, 0, 0).magnitude)
    expectResult(1)(v(1, 0, 0).magnitude)
    expectResult(1)(v(0, 1, 0).magnitude)
    expectResult(1)(v(0, 0, 1).magnitude)
    expectResult(5)(v(0, 3, 4).magnitude)
    expectResult(9)(v(8, 4, 1).magnitude)
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
    expectResult(v(0, 0, 0))(
      v(0, 0, 0).normalize)
  }
  test("Normalize1") {
    expectResult(v(1, 0, 0))(
      v(2, 0, 0).normalize)
  }
  test("Normalize2") {
    expectResult(v(0, 1, 0))(
      v(0, 2, 0).normalize)
  }
  test("Normalize3") {
    expectResult(v(0, 0, 1))(
      v(0, 0, 2).normalize)
  }
  test("Correct") {
    expectResult(v(0.1, 0.1, 0.1))(
      v(0.1, 0.1, 0.1).correct)
    expectResult(v(0, 0, 0))(
      v(1e-16, 1e-16, 1e-16).correct)
  }
  test("Invert") {
    expectResult(v(-1, -2, -3))(
      v(1, 2, 3).invert)
  }
  test("RotateX") {
    expectResult(v(0, -1, 0))(
      v(0, 0, 1).rotateX(90))
    expectResult(v(0, 1, 0))(
      v(0, 0, 1).rotateX(-90))
    // maybe someday we'll make this come out to -1.0 - ST 3/19/08
    expectResult(v(1, -0.9999999999999999, 1))(
      v(1, 1, 1).rotateX(90))
  }
}
