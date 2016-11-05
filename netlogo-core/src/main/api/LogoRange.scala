package org.nlogo.api

import java.lang.{Double => JDouble, StrictMath}

import org.nlogo.core.LogoList

class LogoRange(val start: Double, val stop: Double, val step: Double = 1.0, val inclusive: Boolean = false)
  extends LogoList(DoubleRange(start, stop, step, inclusive))

case class DoubleRange(start: Double, stop: Double, step: Double = 1.0, inclusive: Boolean = false)
  extends IndexedSeq[JDouble] {
  if (step == 0) throw new IllegalArgumentException("`step` must be nonzero")

  // Calculate the scale of precision of the number of the quotient that
  // determines the number of elements in the sequence. Error can occur in the
  // last bit of this number. Note, we may want to extend this to the last two
  // bits due to accumulated error from multiple operations.
  val exponentPrecision = doubleScale((stop - start) / step)
  // So we get a number that essentially has a one at that bit...
  val epsilon = StrictMath.pow(2, -52) * StrictMath.pow(2, exponentPrecision)

  override val length = {
    val n = if (inclusive)
      // We want, for instance, 9.999999999999998 to become 10 instead of 9
      // from the floor, so we add the epsilon here.
      StrictMath.floor((stop - start) / step + epsilon + 1)
    else
      // We want, for instance, 10.000000000000002 to become 10 instead of 11
      // from the ceil, so we subtract the epsilon here.
      StrictMath.ceil((stop - start) / step - epsilon)
    StrictMath.max(n, 0).toInt
  }


  override def apply(idx: Int): JDouble =
    if (0 <= idx && idx <= length) idx * step + start
    else throw new IndexOutOfBoundsException(idx.toString)

  override def tail = DoubleRange(start + step, stop, step, inclusive)
  override def init = DoubleRange(start, stop - step, step, inclusive)

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
      val bits = JDouble.doubleToRawLongBits(x)
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
