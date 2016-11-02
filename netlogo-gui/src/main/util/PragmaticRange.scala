package org.nlogo.util

object PragmaticRange {
  def apply(start: Double = 0.0, stop: Double, step: Double = 1.0) = start until (stop, step)

  /**
    * Extracts the exponent portion of double precision number, correcting for
    * the zero offset.
    */
  def doubleExponent(x: Double) = {
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
