package org.nlogo.lab

import scala.math.sqrt

object StatsCalculator {
  def mean(vals: List[Double]) = {
    vals.sum / vals.length
  }


  // use double nan if not well defined. Display "N/A"
  def std(vals: List[Double]): Double = {
    if (vals.length > 2) {
      val avg = mean(vals)
      sqrt( vals.map(v => (v - avg) * (v - avg)).sum / vals.length )
    } else {
      Double.NaN
    }
  }
}
