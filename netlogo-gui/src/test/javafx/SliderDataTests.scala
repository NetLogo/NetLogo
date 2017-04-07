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
      val s = new SliderData(50, x, 0.0, 0.0)
      assertResult(expectedPrecision)(s.precision)
    }
  }

  def testPrecision(x: Double, y: Double, expectedPrecision: Int): Unit = {
    test(s"precision of min: $x, inc: $y is $expectedPrecision") {
      val s = new SliderData(50, x, 0.0, y)
      assertResult(expectedPrecision)(s.precision)
    }
  }

  def testEffectiveMax(expectedMax: Double)(min: Double, inc: Double, max: Double): Unit = {
    test(s"effective maximum of ($min, $max) with increment $inc is $expectedMax") {
      val s = new SliderData(50, min, max, inc)
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

  test("binds input to value through a value coercion") {
    val d = new SliderData(50, 75.0, 388.0, 0.1)
    d.inputValueProperty.set(76.0)
    assertResult(76.0)(d.value)
  }

  test("updateFromModel overrides bound values") {
    val d = new SliderData(5, 5, 10.0, 0.1)
    d.inputValueProperty.set(-3.0)
    assertResult(5)(d.value)
    d.updateFromModel(-5)
    assertResult(-5)(d.value)
  }

  class SliderStateMachine {
    var currentValue = 0
    var userIsChanging = false
    var changingTags = Set.empty[String]

    def valueSendStarted(t: String): Unit = {
      changingTags += t
    }
    def valueSendFinished(t: String): Unit = {
      changingTags -= t
    }
    def valueReceivedFromModel(i: Int): Unit = {
      if (! userIsChanging) {
        currentValue = i
      }
    }
    def userStartedChanging(): Unit = {
      userIsChanging = true
    }
    def userChangeFinished(i: Int): Unit = {
      userIsChanging = false
      currentValue = i
    }
  }

  trait StateTest {
    val s = new SliderStateMachine()
  }

  test("State machine reflects model value") { new StateTest {
    s.valueReceivedFromModel(2)
    assertResult(2)(s.currentValue)
  } }

  test("updates for user changes") { new StateTest {
    s.userStartedChanging()
    s.userChangeFinished(2)
    assertResult(2)(s.currentValue)
  } }

  test("does not accept updates from the model while changing") { new StateTest {
    s.userStartedChanging()
    s.valueReceivedFromModel(2)
    assertResult(0)(s.currentValue)
  } }

  test("accepts updates from the model once the user is done changing") { new StateTest {
    s.userStartedChanging()
    s.userChangeFinished(1)
    s.valueReceivedFromModel(2)
    assertResult(2)(s.currentValue)
  } }

  // don't know whether this is right or not
  test("only updates after model updates are finished") { new StateTest {
    s.valueSendStarted("abc")
    s.valueReceivedFromModel(2)
    assertResult(0)(s.currentValue)
    s.valueSendFinished("abc")
    assertResult(2)(s.currentValue)
  } }
}

class SliderDataPropTests extends PropSpec with PropertyChecks {
  def fiftyProperty = new SimpleDoubleProperty(50d)

  implicit def arbSlider: Arbitrary[SliderData] = Arbitrary(genSlider)
  val genSlider: Gen[SliderData] = for {
    min <- Gen.choose(0, 100)
    max <- Gen.choose(50, 500) // make max usually bigger than min, but not always.
    inc <- Gen.oneOf(0.0, 0.1, 0.01, 1.0, 2.0)
  } yield new SliderData(50d, min, max, inc)

  property("value is never higher than max(min,max)") {
    forAll((s: SliderData, d: Double) =>
      whenever(d > 0) {
        s.inputValueProperty.set(s.maximum + d)
        assert(s.value <= math.max(s.minimum, s.maximum))})}

  property("value is never lower than min(min,max)") {
    forAll((s: SliderData, d: Double) =>
      whenever(d > 0) {
        s.inputValueProperty.set(s.minimum - d)
        assert(s.value >= math.min(s.minimum, s.maximum))})}

  property("values between max and min are set") {
    forAll((s: SliderData, i: Int) =>
        whenever(s.maximum > (s.minimum + s.increment) && s.increment > 0 && i > 0) {
          s.inputValueProperty.set(s.maximum + (i * s.increment))
          assert(s.value > s.minimum)
          assert(s.value <= s.maximum)
        })
  }

  property("effective maximum is never greater than max") {
    forAll((s: SliderData) =>
        whenever(s.minimum <= s.maximum) { assert(s.effectiveMaximum <= s.maximum) })
  }

  property("precision for ints should always be zero") {
    forAll((i: Int) =>
        whenever(i < 100000000) {
          assertResult(0)(new SliderData(50, i, i, 1).precision)
        })
  }

  property("when min > max, effective max always min") {
    forAll((min: Double, max: Double) =>
        whenever(min > max) {
          val s = new SliderData(50, min, max, 1)
          assertResult(s.minimum)(s.effectiveMaximum)
        })
  }

  property("Slider coerces value to increment") {
    forAll { (d: Double) =>
      whenever(d > 0) {
        val s = new SliderData(0, 0, d + 10, 1)
        s.inputValueProperty.set(d)
        assertResult(0.0)(StrictMath.IEEEremainder(s.value, 1.0))
      }
    }
  }

  property("when inc == 0, effective max is always min") {
    forAll((min: Double) =>
        assertResult(min)(
          new SliderData(50, min, 100, 0).effectiveMaximum))
  }

  property("Slider has same values regardless of set order") {
    forAll { (s: SliderData, d: Double) =>
        val defaultSlider = new SliderData(50, 0, 100, 1)
        defaultSlider.inputValueProperty.set(d)
        defaultSlider.minimumProperty.setValue(s.minimum)
        defaultSlider.maximumProperty.setValue(s.maximum)
        defaultSlider.incrementProperty.setValue(s.increment)
        s.inputValueProperty.set(d)
        assertResult(s.value)(defaultSlider.value)
    }
  }

  property("Slider respects value set from model, if recently updated") {
    forAll { (s: SliderData, inputValue: Double, modelValue: Double) =>
      s.inputValueProperty.set(inputValue)
      assert(s.value >= s.minimum)
      assert(s.value <= StrictMath.max(s.minimum, s.maximum))
      s.updateFromModel(modelValue)
      assertResult(modelValue)(s.value)
    }
  }

}
