package org.nlogo.api

import annotation.strictfp

object Pow {

  @strictfp
  def pow(d0: Double, d1: Double) =
    if (d1 == 0)
      1.0
    else {
      // If there is some more efficient way to test whether a double has no fractional part and
      // lies in IEEE 754's exactly representable range, I don't know it - ST 5/31/06
      var n = d1.toLong
      if (n != d1 || n < -9007199254740992L || n > 9007199254740992L)
        StrictMath.pow(d0, d1)
      else {
        var d = d0
        if (n < 0) {
          d = 1 / d
          n = -n
        }
        // en.wikipedia.org/wiki/Exponentiation_by_squaring
        var result = 1.0
        while (n > 0) {
          if (n % 2 == 1)
            result *= d
          d *= d
          n /= 2
        }
        result
      }
    }

}
