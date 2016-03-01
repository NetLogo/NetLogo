// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api.Numbers.Infinitesimal

@annotation.strictfp
/**
 * Cache of sines and cosines of integers 0-359.
 * Speed up various operations on angles that are integers.  Trigonometry is expensive!
 */
object TrigTables {
  val sin = Array.tabulate(360)(mySin)
  val cos = Array.tabulate(360)(myCos)
  private def mySin(n: Int): Double = {
    val radians = StrictMath.toRadians(n)
    val x = StrictMath.sin(radians)
    if (StrictMath.abs(x) < Infinitesimal)
      0
    else
      x
  }
  private def myCos(n: Int): Double = {
    val radians = StrictMath.toRadians(n)
    val x = StrictMath.cos(radians)
    if (StrictMath.abs(x) < Infinitesimal)
      0
    else
      x
  }
}
