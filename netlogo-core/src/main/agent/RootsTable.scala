// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import it.unimi.dsi.fastutil.BigArrays
import it.unimi.dsi.fastutil.doubles.DoubleBigArrays

// table lookup of square roots
//
// StrictMath.sqrt is expensive, so doing a table lookup instead
// speeds up distance calculations between patch centers greatly

class RootsTable(worldWidth: Int, worldHeight: Int) {
  private val size: Long = worldWidth * worldWidth + worldHeight * worldHeight
  private val rootsTable = DoubleBigArrays.newBigArray(size)

  for (i <- 0L until size)
    BigArrays.set(rootsTable, i, StrictMath.sqrt(i.toDouble))

  def gridRoot(x: Double): Double = {
    val i = x.toInt

    if (x == i) {
      gridRoot(i)
    } else {
      StrictMath.sqrt(x)
    }
  }

  def gridRoot(n: Int): Double = {
    if (n < size) {
      BigArrays.get(rootsTable, n)
    } else {
      StrictMath.sqrt(n)
    }
  }
}
