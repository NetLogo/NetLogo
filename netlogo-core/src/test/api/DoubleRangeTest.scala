package org.nlogo.api

import org.scalatest.FunSuite

import scala.math.pow

class DoubleRangeTest extends FunSuite {
  // Tests are given as:
  // (start, stop, step, expected length of list)
  val exclusiveCases = Seq[(Double, Double, Double, Int)](
    (0.0, 10.0, 1.0, 10),
    (0.0, 9.0, 2.0, 5),
    (-1.0 + 1.0 / 3.0, 0, 1.0 / 3.0, 2), // Shows approaching 0
    (pow(2, 52) - 100.0, pow(2,52), 1.0, 100), // tests maximal differences in exponent
    (pow(2, 52) - 100000.0, pow(2, 52), 1000.0, 100),
    (-2.0, pow(2,51), pow(2,51), 2),
    (0.0, 1.0, 0.1, 10),
    (1e-4, 1e-3, 1e-4, 9),
    (1.00001e-10, 1e-9, 1e-10, 9),
    (0.0, 1.0, 1.0 / 3.0, 3),
    (1.0 / 3.0, 1, 1.0 / 3.0, 2),
    (0.0, 2.0, 2.0 / 3.0, 3),
    (2.0 / 3.0, 2, 2.0 / 3.0, 2),
    (0.0, 1.0, 1.0 / 6.0, 6),
    (1.0 / 6.0, 1, 1.0 / 6.0, 5),
    (0, 3333.333334, 1.0 / 3.0, 10001),
    (3.0, 2.3, -0.1, 7)
  )
  val inclusiveCases = Seq[(Double, Double, Double, Int)](
    (0.0, 10.0, 1.0, 11),
    (0.0, 9.0, 2.0, 5),
    (-1.0 + 1.0 / 3.0, 0, 1.0 / 3.0, 3), // Shows approaching 0
    (pow(2, 52) - 100.0, pow(2,52), 1.0, 101), // tests maximal differences in exponent
    (pow(2, 52) - 100000.0, pow(2, 52), 1000.0, 101),
    (-2.0, pow(2,52), pow(2,52), 2),
    (0.0, 1.0, 0.1, 11),
    (1e-4, 1e-3, 1e-4, 10),
    (1.00001e-10, 1e-9, 1e-10, 9),
    (0.0, 1.0, 1.0 / 3.0, 4),
    (1.0 / 3.0, 1, 1.0 / 3.0, 3),
    (0.0, 2.0, 2.0 / 3.0, 4),
    (2.0 / 3.0, 2, 2.0 / 3.0, 3),
    (0.0, 1.0, 1.0 / 6.0, 7),
    (1.0 / 6.0, 1, 1.0 / 6.0, 6),
    (0, 3333.333334, 1.0 / 3.0, 10001),
    (3.0, 2.3, -0.1, 8)
  )

  def scale(s: Seq[(Double, Double, Double, Int)]) = s.map {
    case (start: Double, stop: Double, step: Double, n: Int) =>
      (start * 0.3, stop * 0.3, step * 0.3, n)
  }
  def invert(s: Seq[(Double, Double, Double, Int)]) = s.map {
    case (start: Double, stop: Double, step: Double, n: Int) =>
      (stop, start, - step, n)
  }


  (exclusiveCases
    ++ scale(exclusiveCases)
    ++ invert(exclusiveCases)
    ++ invert(scale(exclusiveCases))).foreach {
    case (start: Double, stop: Double, step: Double, n: Int) =>
      test(s"testing from $start until $stop by $step gives $n numbers") {
        assertResult(n)(DoubleRange(start, stop, step).size)
      }
  }
  (inclusiveCases
    ++ scale(inclusiveCases)
    ++ invert(inclusiveCases)
    ++ invert(scale(inclusiveCases))).foreach {
    case (start: Double, stop: Double, step: Double, n: Int) =>
      test(s"testing from $start through $stop by $step gives $n numbers") {
        assertResult(n)(DoubleRange(start, stop, step, inclusive = true).size)
      }
  }
}
