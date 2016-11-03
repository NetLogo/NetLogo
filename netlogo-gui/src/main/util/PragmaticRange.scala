package org.nlogo.util

import java.lang.StrictMath.{pow, min}

/**
  * An implementation of `Range`-like functionality that tries to be as close
  * as possible to what user intends by correcting for error introduced by
  * floating-point arithmetic.
  */
object PragmaticRange {
  def apply(stop: Double): Vector[Double] = PragmaticRange(0.0, stop)
  def apply(start: Double, stop: Double): Vector[Double] = PragmaticRange(start, stop, 1.0)
  def apply(start: Double, stop: Double, step: Double): Vector[Double] = {
    if (step == 0) throw new IllegalArgumentException("`step` must be nonzero")

    val builder = Vector.newBuilder[Double]

    val sign = if (step < 0) -1 else 1

    // Calculate the smallest level of precision specified by `start` and
    // `stop`. This tells us at scale error can occur.
    val exponentPrecision = min(doubleScale(start), doubleScale(stop))

    // `epsilon` will operate on the last two bits of the significand of
    // a double at that level of precision. This is the scale at which we
    // tolerate error.
    val epsilon = if (doubleScale(step) + 50 < exponentPrecision)
      // the scale of step is close to or below the scale of start and stop,
      // so epsilon will be too large. In this case, we don't bother trying to
      // correct since there's not much we can do.
      0
    else
      (pow(2, -51) + pow(2, -52)) * pow(2, exponentPrecision)

    var i = 0
    while (sign * (i * step + start) + epsilon < sign * stop) {
      // Using multiplication to calculate each number rather than repeated
      // addition prevents error from accumulating.
      builder += i * step + start
      i += 1
    }
    builder.result
  }

  /**
    * Quantifies the scale of the given double. Usually this is the exponent
    * portion of the double bit representation, corrected for its zero offset.
    * 0 is given a scale of 1023, however, so that it's always considered less
    * specific, so to speak, than any other number.
    */
  private def doubleScale(x: Double): Long = {
    if (x == 0)
      1023
    else {
      val bits = java.lang.Double.doubleToRawLongBits(x)
      // Cut off the significand, which is the last 52 bits
      val rightShifted = bits >>> 52
      // This leaves us with 12 bits, where the first one is the sign and the
      // last 11 are the exponent + 1023
      // So, we use an 11 bit mask to drop the sign.
      val mask = (1L << 11) - 1L
      // And then subtract 1023 to correct for the zero offset (0 is -1023)
      (rightShifted & mask) - 1023
    }
  }

}
