package org.nlogo.window

import org.scalacheck.Prop._
import org.scalatest.FunSuite
import org.scalatest.prop.Checkers
import org.scalacheck.{Arbitrary, Gen}
import org.nlogo.agent.ConstantSliderConstraint

class SliderDataTests extends FunSuite with Checkers {

  implicit def arbSlider: Arbitrary[SliderData] = Arbitrary(genSlider)
  val genSlider: Gen[SliderData] = for {
    min <- Gen.choose(0,100)
    max <- Gen.choose(50,500) // make max usually bigger than min, but not always.
    inc <- Gen.oneOf(0.0, 0.1, 0.01, 1.0, 2.0)
  } yield new SliderData(min,max,inc)

  // set value tests
  test("after setting constraints, value is never higher than max(min,max)") {
    check((s:SliderData, d:Double) => (d>0) ==> {
      s.value = s.maximum + d
      // some value on the constraint must change, or the value isnt updated.
      // im not actually sure if this is a good rule or not. 
      s.setSliderConstraint(new ConstantSliderConstraint(s.minimum - 1, s.maximum, s.increment))
      s.value <= math.max(s.minimum, s.maximum)
    })
  }

  //
  // precision tests.
  //
  test("double precision") {
    check((right: Int) => {
      val r = (right * 2 + 1).abs // get an odd number so it doesn't end in zero.
      // all these tests use 1.r
      new SliderData(minimum = (1 + "." + r).toDouble).precision == r.toString.length
    })
  }

  test("precision for ints should always be zero") {
    check((i: Int) => ! i.toDouble.toString.contains("E") ==> {
      new SliderData(minimum = i).precision == 0
    })
  }

  test("sanity precision tests"){
    def testPrecision(d:Double, expected:Int){ assert(new SliderData(minimum = d).precision === expected) }
    testPrecision(1.1, 1)
    testPrecision(1.0, 0)
    testPrecision(1.01, 2)
    testPrecision(1.10, 1)
    testPrecision(83242938.83242938, 8)
    testPrecision(1.23E0, 2) // 1.23
    testPrecision(1.23E2, 0) // 123.0
    testPrecision(1.23E5, 0) // 12300.0
    testPrecision(1.234E2, 1) // 123.4
    testPrecision(1.2340000, 3) // 1.234

    def testPrecision2(minimum:Double, increment: Double, expected:Int){
      assert(new SliderData(minimum = minimum, increment=increment).precision === expected)
    }

    // these tests make sure the highest precision is taken
    // because thats what the implementation does.
    // why it does it I'm unsure (JC - 9/27/10)
    testPrecision2(minimum=0.1, increment=0.01, expected=2)
    testPrecision2(minimum=0.01, increment=0.1, expected=2)
    testPrecision2(minimum=0, increment=0.1, expected=1)
    testPrecision2(minimum=0.1, increment=0, expected=1)
    testPrecision2(minimum=0, increment=0.001, expected=3)
    testPrecision2(minimum=0.001, increment=0, expected=3)
  }

  //
  // effective max tests
  //
  test("effective max sanity tests"){
    assert(new SliderData(minimum=0,maximum=100,increment=1).effectiveMaximum === 100.0)
    assert(new SliderData(minimum=25,maximum=50,increment=1).effectiveMaximum === 50.0)
    assert(new SliderData(minimum=0.1,maximum=50,increment=0.1).effectiveMaximum === 50.0)
    assert(new SliderData(minimum=25,maximum=50,increment=2).effectiveMaximum === 49.0)
    assert(new SliderData(minimum=25,maximum=50,increment=3).effectiveMaximum === 49.0)
    assert(new SliderData(minimum=25,maximum=50,increment=7).effectiveMaximum === 46.0)
  }

  test("when min > max, effective max is always min") {
    check((min: Double, max: Double) => (min > max) ==> {
      new SliderData(minimum=min, maximum=max).effectiveMaximum == min
    })
  }

  test("when inc == 0, effective max is always min") {
    check((min: Double) => { new SliderData(minimum=min, increment=0).effectiveMaximum == min })
  }

  test("effective maximum is never greater than max") {
    check((s:SliderData) => (s.minimum <= s.maximum) ==> { s.effectiveMaximum <= s.maximum })
  }
}
