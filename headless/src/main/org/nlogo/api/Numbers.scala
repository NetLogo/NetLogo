// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

object Numbers {
  // 9007199254740992 is the largest/smallest integer
  // exactly representable in a double - ST 1/29/08
  def isValidLong(d: Double): Boolean =
    d <= 9007199254740992L && d >= -9007199254740992L
}
