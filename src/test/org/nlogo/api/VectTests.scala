// (C) 2011 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.scalatest.FunSuite

class VectTests extends FunSuite {
  def v(x: Double, y: Double, z: Double) = new Vect(x, y, z)
  test("ToString") {
    expect("Vect(1.0,2.0,3.0)")(v(1, 2, 3).toString)
    expect("Vect(1.1,2.2,3.3)")(v(1.1, 2.2, 3.3).toString)
  }
  test("Equality") {
    assert(v(1, 2, 3) == v(1, 2, 3))
  }
  test("Magnitude") {
    expect(0)(v(0, 0, 0).magnitude)
    expect(1)(v(1, 0, 0).magnitude)
    expect(1)(v(0, 1, 0).magnitude)
    expect(1)(v(0, 0, 1).magnitude)
    expect(5)(v(0, 3, 4).magnitude)
    expect(9)(v(8, 4, 1).magnitude)
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
    expect(v(0, 0, 0))(
      v(0, 0, 0).normalize)
  }
  test("Normalize1") {
    expect(v(1, 0, 0))(
      v(2, 0, 0).normalize)
  }
  test("Normalize2") {
    expect(v(0, 1, 0))(
      v(0, 2, 0).normalize)
  }
  test("Normalize3") {
    expect(v(0, 0, 1))(
      v(0, 0, 2).normalize)
  }
  test("Correct") {
    expect(v(0.1, 0.1, 0.1))(
      v(0.1, 0.1, 0.1).correct)
    expect(v(0, 0, 0))(
      v(1e-16, 1e-16, 1e-16).correct)
  }
  test("Invert") {
    expect(v(-1, -2, -3))(
      v(1, 2, 3).invert)
  }
  test("RotateX") {
    expect(v(0, -1, 0))(
      v(0, 0, 1).rotateX(90))
    expect(v(0, 1, 0))(
      v(0, 0, 1).rotateX(-90))
    // maybe someday we'll make this come out to -1.0 - ST 3/19/08
    expect(v(1, -0.9999999999999999, 1))(
      v(1, 1, 1).rotateX(90))
  }
}
