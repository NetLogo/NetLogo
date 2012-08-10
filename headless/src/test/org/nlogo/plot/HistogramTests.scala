// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

import org.scalatest.FunSuite

class HistogramTests extends FunSuite {
  test("empty histogram") {
    val histogram = new Histogram(0, 3, 1)
    expect(0)(histogram.ceiling)
    expect(List(0, 0, 0))(histogram.bars.toList)
  }
  test("integer histogram") {
    val histogram = new Histogram(0, 3, 1)
    List(0.0, 0.5, 0.999999999, 1.0, 1.999999999, 2.0).foreach(histogram.nextValue)
    expect(3)(histogram.ceiling)
    expect(List(3, 2, 1))(histogram.bars.toList)
  }
  test("out of bounds values") {
    val histogram = new Histogram(0, 3, 1)
    histogram.nextValue(-0.000001)
    histogram.nextValue(3)
    expect(0)(histogram.ceiling)
  }
  test("floating point pitfall") {
    val histogram = new Histogram(0, 1, 0.1)
    (0 until 10).foreach(n => histogram.nextValue(n / 10.0))
    expect(1)(histogram.ceiling)
    expect(10)(histogram.bars.size)
    assert(histogram.bars.forall(_ == 1))
  }
  test("small range") {
    val histogram = new Histogram(0, 0.5, 0.1)
    List(0.0, 0.1, 0.2).foreach(histogram.nextValue)
    expect(1)(histogram.ceiling)
    expect(List(1, 1, 1, 0, 0))(histogram.bars.toList)
  }
}
