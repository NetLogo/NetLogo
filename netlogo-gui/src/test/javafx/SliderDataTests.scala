// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.javafx

import javafx.beans.property.SimpleDoubleProperty

import org.scalacheck.{ Arbitrary, Gen }

import org.scalatest.{ FunSuite, PropSpec }
import org.scalatest.prop.PropertyChecks

class SliderDataTests extends FunSuite {

  def fiftyProperty = new SimpleDoubleProperty(50d)

  def testPrecision(x: Double, expectedPrecision: Int): Unit = {
    test(s"precision of min: $x is $expectedPrecision") {
      val s = new SliderData(fiftyProperty, x, 0.0, 0.0)
      assertResult(expectedPrecision)(s.precision)
    }
  }

  def testPrecision(x: Double, y: Double, expectedPrecision: Int): Unit = {
    test(s"precision of min: $x, inc: $y is $expectedPrecision") {
      val s = new SliderData(fiftyProperty, x, 0.0, y)
      assertResult(expectedPrecision)(s.precision)
    }
  }

  def testEffectiveMax(expectedMax: Double)(min: Double, inc: Double, max: Double): Unit = {
    test(s"effective maximum of ($min, $max) with increment $inc is $expectedMax") {
      val s = new SliderData(fiftyProperty, min, max, inc)
      assertResult(expectedMax)(s.effectiveMaximum)
    }
  }

  testPrecision(0, 0)
  testPrecision(1.0, 0)
  testPrecision(1.1, 1)
  testPrecision(1.01, 2)
  testPrecision(83242938.83242938, 8)
  testPrecision(1.23E0, 2) // 1.23
  testPrecision(1.23E2, 0) // 123.0
  testPrecision(1.23E5, 0) // 12300.0
  testPrecision(1.234E2, 1) // 123.4
  testPrecision(1.2340000, 3) // 1.234

  testPrecision(0.1,   0.01,  2)
  testPrecision(0.01,  0.1,   2)
  testPrecision(0,     0.1,   1)
  testPrecision(0.1,   0,     1)
  testPrecision(0,     0.001, 3)
  testPrecision(0.001, 0,     3)

  // testEffectiveMax
  // min inc max expected
  // \1 \3 \2
  testEffectiveMax(100.0)(min = 0,  inc = 1,   max = 100)
  testEffectiveMax(50.0)(min = 25,  inc = 1,   max = 50)
  testEffectiveMax(50.0)(min = 0.1, inc = 0.1, max = 50)
  testEffectiveMax(49.0)(min = 25,  inc = 2,   max = 50)
  testEffectiveMax(49.0)(min = 25,  inc = 3,   max = 50)
  testEffectiveMax(46.0)(min = 25,  inc = 7,   max = 50)
  testEffectiveMax(391.0)(min = 28, inc = 0.01, max = 391.0)


  test("coerce value....") {
    pending
  }
}

class SliderDataPropTests extends PropSpec with PropertyChecks {
  def fiftyProperty = new SimpleDoubleProperty(50d)

  implicit def arbSlider: Arbitrary[SliderData] = Arbitrary(genSlider)
  val genSlider: Gen[SliderData] = for {
    min <- Gen.choose(0, 100)
    max <- Gen.choose(50, 500) // make max usually bigger than min, but not always.
    inc <- Gen.oneOf(0.0, 0.1, 0.01, 1.0, 2.0)
  } yield new SliderData(new SimpleDoubleProperty(50d), min, max, inc)

  property("value is never higher than max(min,max)") {
    forAll((s: SliderData, d: Double) =>
      whenever(d > 0) {
        s.inputValue.set(s.maximum + d)
        assert(s.value.get <= math.max(s.minimum, s.maximum))})}

  property("value is never lower than min(min,max)") {
    forAll((s: SliderData, d: Double) =>
      whenever(d > 0) {
        s.inputValue.set(s.minimum - d)
        assert(s.value.get >= math.min(s.minimum, s.maximum))})}

  property("values between max and min are set") {
    forAll((s: SliderData, i: Int) =>
        whenever(s.maximum > s.minimum && i > 0) {
          s.inputValue.set(s.maximum + (i * s.increment))
          assert(s.value.get > s.minimum)
          assert(s.value.get < s.maximum)
        })
  }

  property("effective maximum is never greater than max") {
    forAll((s: SliderData) =>
        whenever(s.minimum <= s.maximum) { assert(s.effectiveMaximum <= s.maximum) })
  }

  property("precision for ints should always be zero") {
    forAll((i: Int) =>
        whenever(! i.toDouble.toString.contains("E")) {
          assertResult(0)(new SliderData(fiftyProperty, i, i, 1).precision)
        })
  }

  property("when min > max, effective max always min") {
    pending
  }
  property("when inc == 0, effective max always min") {
    pending
  }
}
