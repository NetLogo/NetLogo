// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

// table lookup of square roots

@annotation.strictfp
class RootsTable(worldWidth: Int, worldHeight: Int) {
  private val rootsTable =
    Array.tabulate(worldWidth * worldWidth + worldHeight * worldHeight)(
      StrictMath.sqrt(_))
  def gridRoot(x: Double): Double = {
    val i = x.toInt
    if(x == i)
      gridRoot(i)
    else
      StrictMath.sqrt(x)
  }
  def gridRoot(n: Int): Double =
    if(n < rootsTable.length)
      rootsTable(n)
    else
      StrictMath.sqrt(n)
}
