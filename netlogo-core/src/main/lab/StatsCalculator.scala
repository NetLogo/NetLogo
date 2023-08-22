package org.nlogo.lab

import scala.math.sqrt

object StatsCalculator {
  def mean(vals: List[Double]) = {
    vals.sum / vals.length
  }

  def std(vals: List[Double]) = {
    val avg = mean(vals)
    sqrt( vals.map(v => (v - avg) * (v - avg)).sum / vals.length )
  }
}