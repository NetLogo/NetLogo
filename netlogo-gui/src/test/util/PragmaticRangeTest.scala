package org.nlogo.util

import org.scalatest.FunSuite
import scala.math.pow

/**
  * Created by bryan on 11/2/16.
  */
class PragmaticRangeTest extends FunSuite {
  // Tests are given as:
  // (start, stop, step, expected length of list)
  val baseCases = Seq[(Double, Double, Double, Int)](
    (0.0, 10.0, 1.0, 10),
    (0.0, 9.0, 2.0, 5),
    (-1.0 + 1.0 / 3.0, 0, 1.0 / 3.0, 2), // Shows approaching 0
    (pow(2, 52) - 100.0, pow(2,52), 1.0, 100), // tests maximal differences in exponent
    (pow(2, 52) - 100000.0, pow(2, 52), 1000.0, 100),
    (-1.0, pow(2,52), pow(2,52), 2),
    (0.0, 1.0, 0.1, 10),
    (1e-4, 1e-3, 1e-4, 9),
    (1.00001e-10, 1e-9, 1e-10, 9),
    (0.0, 1.0, 1.0 / 3.0, 3),
    (1.0 / 3.0, 1, 1.0 / 3.0, 2),
    (0.0, 2.0, 2.0 / 3.0, 3),
    (2.0 / 3.0, 2, 2.0 / 3.0, 2),
    (0.0, 1.0, 1.0 / 6.0, 6),
    (1.0 / 6.0, 1, 1.0 / 6.0, 5),
    (0, 3333.333334, 1.0 / 3.0, 10001)
  )
  val translatedCases = baseCases.map {
    case (start: Double, stop: Double, step: Double, n: Int) =>
      (start + 100000, stop + 100000, step, n)
  }

  val invertedCases = (baseCases ++ translatedCases).map {
    case (start: Double, stop: Double, step: Double, n: Int) =>
      (stop, start, - step, n)
  }

  (baseCases ++ translatedCases ++ invertedCases).foreach {
    case (start: Double, stop: Double, step: Double, n: Int) =>
      test(s"testing from $start to $stop by $step gives $n numbers") {
        assertResult(n)(PragmaticRange(start, stop, step).size)
      }
  }
}
