package org.nlogo.api

case class DoubleRange(start: Double, stop: Double, step: Double = 1.0, inclusive: Boolean = false)
  extends IndexedSeq[Double] {
  if (step == 0) throw new IllegalArgumentException("`step` must be nonzero")

  override val length: Int = {
    val l = if (inclusive) {
      // We want, for instance, 9.999999999999998 to become 10 instead of 9
      // from the floor, so we add the ulp here.
      val num = (stop - start) / step + 1
      StrictMath.floor(num + 2 * StrictMath.ulp(num))
    } else {
      // We want, for instance, 10.000000000000002 to become 10 instead of 11
      // from the ceil, so we subtract the ulp here.
      val num = (stop - start) / step
      StrictMath.ceil(num - 2 * StrictMath.ulp(num))
    }

    if (l > Integer.MAX_VALUE)
      throw new IllegalArgumentException("Range results in too many elements.")

    StrictMath.max(l, 0).toInt
  }


  override def apply(idx: Int): Double =
    if (0 <= idx && idx < length) idx * step + start
    else throw new IndexOutOfBoundsException(idx.toString)

  override def tail = DoubleRange(start + step, stop, step, inclusive)
  override def init = DoubleRange(start, stop - step, step, inclusive)
}
