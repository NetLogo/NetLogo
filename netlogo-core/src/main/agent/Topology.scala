// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api, api.AgentException

@annotation.strictfp
object Topology {

  // factory method
  def get(world: World2D, xWraps: Boolean, yWraps: Boolean): Topology =
    (xWraps, yWraps) match {
      case (true , true ) => new Torus(world)
      case (true , false) => new VertCylinder(world)
      case (false, true ) => new HorizCylinder(world)
      case (false, false) => new Box(world)
    }

  // General wrapping function.
  def wrap(pos: Double, min: Double, max: Double): Double =
    if (pos >= max)
      min + ((pos - max) % (max - min))
    else if (pos < min) {
      val result = max - ((min - pos) % (max - min))
      // careful, if d is infinitesimal, then (max - d) might actually equal max!
      // but we must return an answer which is strictly less than max - ST 7/20/10
      if (result < max)
        result else min
    }
    else pos

  // for when you have a pcor and/or an offset and want to
  // wrap without converting to a double.
  def wrapPcor(pos: Int, min: Int, max: Int): Int =
    if (pos > max)
      min + ((pos - min) % (1 + max - min))
    else if (pos < min) // NOTE: we assume that min <= 0 here, since the world must always contain (0, 0)
      ((pos - max) % (1 + max - min)) + max
    else
      pos

}

abstract class Topology(val world: World, val xWraps: Boolean, val yWraps: Boolean)
extends Neighbors {

  @throws(classOf[AgentException])
  def wrapX(x: Double): Double
  @throws(classOf[AgentException])
  def wrapY(y: Double): Double

  def distanceWrap(dx: Double, dy: Double, x1: Double, y1: Double, x2: Double, y2: Double): Double
  def towardsWrap(headingX: Double, headingY: Double): Double

  def shortestPathX(x1: Double, x2: Double): Double
  def shortestPathY(y1: Double, y2: Double): Double

  ///

  def followOffsetX: Double = world.observer.followOffsetX
  def followOffsetY: Double = world.observer.followOffsetY

  @throws(classOf[AgentException])
  @throws(classOf[PatchException])
  def diffuse(amount: Double, vn: Int): Unit = {
    val scratch = getPatchScratch(vn)
    diffuseCenter (amount, vn, fourWay = false, scratch)
    diffuseXBorder(amount, vn, fourWay = false, scratch)
    diffuseYBorder(amount, vn, fourWay = false, scratch)
    diffuseCorners(amount, vn, fourWay = false, scratch)
  }

  @throws(classOf[AgentException])
  @throws(classOf[PatchException])
  def diffuse4(amount: Double, vn: Int): Unit = {
    val scratch = getPatchScratch(vn)
    diffuseCenter (amount, vn, fourWay = true, scratch)
    diffuseXBorder(amount, vn, fourWay = true, scratch)
    diffuseYBorder(amount, vn, fourWay = true, scratch)
    diffuseCorners(amount, vn, fourWay = true, scratch)
  }

  protected def diffuseCenter(amount: Double, vn: Int, fourWay: Boolean, scratch: Array[Array[Double]]): Unit = {
    val lastX = world.worldWidth - 1
    val lastY = world.worldHeight - 1
    var x = 1
    while (x < lastX) {
      val e = scratch(x+1)
      val c = scratch(x)
      val w = scratch(x-1)
      var y = 1
      while (y < lastY) {
        val oldVal = c(y)
        val sum = sum4(e(y), w(y), c(y - 1), c(y + 1))
        if (fourWay)
          updatePatch(amount, vn, 4, x, y, oldVal, sum)
        else
          updatePatch(amount, vn, 8, x, y, oldVal,
            sum + sum4(e(y+1), e(y-1), w(y+1), w(y-1)))
        y += 1
      }
      x += 1
    }
  }

  protected def diffuseXBorder(amount: Double, vn: Int, fourWay: Boolean, scratch: Array[Array[Double]]): Unit
  protected def diffuseYBorder(amount: Double, vn: Int, fourWay: Boolean, scratch: Array[Array[Double]]): Unit
  protected def diffuseCorners(amount: Double, vn: Int, fourWay: Boolean, scratch: Array[Array[Double]]): Unit

  /**
    * This method returns a consistent sum of the four numbers regardless of their order.
    *
    * Floating point addition is commutative but NOT associative. Hence, the sum of a set of numbers will change
    * depending on their order. This procedure fixes that by summing the numbers as: (2 lowest) + (2 highest).
    * The naive fix to this problem is to sort the numbers before summing. However, this is slow and unnecessary.
    * Instead, we split the numbers into two groups, such that each group contains either the two highest or the two
    * lowest numbers (though we don't necessarily know which group is which). We then sum each group separately, and
    * then sum their sums. This works because we can rely on the commutativity of floats. -- BCH 10/15/2017
    */
  protected def sum4(a: Double,b: Double,c: Double,d: Double): Double = {
    var low1, high1, low2, high2: Double = 0.0
    if (a < b) {
      low1 = a
      high1 = b
    } else {
      low1 = b
      high1 = a
    }

    if (c < d) {
      low2 = c
      high2 = d
    } else {
      low2 = d
      high2 = c
    }

    if ((low2 < high1) && (low1 < high2))
      // Covers
      // (low1 <> low2) < (high1 <> high2)
      (low1 + low2) + (high1 + high2)
    else
      // Covers
      // (low1 < high1) < (low2 < high2)
      // (low1 < high1) > (low2 < high2)
      (low1 + high1) + (low2 + high2)
  }

  protected def updatePatch(amount: Double, vn: Int, directions: Int,
                            x: Int, y: Int, oldVal: Double, sum: Double): Unit = {
    val newVal = oldVal + amount * (sum / directions - oldVal)
    if (newVal != oldVal) {
      world.patches.getByIndex(y * world.worldWidth + x).setPatchVariable(vn, Double.box(newVal))
    }
  }

  protected def getPatchScratch(vn: Int): Array[Array[Double]] = {
    val xx = world.worldWidth
    val yy = world.worldHeight
    val minX = world.minPxcor
    val maxY = world.maxPycor
    val scratch = world.getPatchScratch
    var i = 0
    val ps = xx * yy
    while (i < ps) {
      val p = world.patches.getByIndex(i).asInstanceOf[Patch]
      scratch(p.pxcor - minX)(maxY - p.pycor) = p.getPatchVariable(vn).asInstanceOf[Double].doubleValue
      i += 1
    }
    scratch
  }

  // getPatch methods.  These are here so they can be called by subclasses in their implementations
  // of getPN, getPS, etc.  They provide the usual torus-style behavior.  It's a little odd that
  // they're here rather than in Torus, but doing it that way would have involved other
  // awkwardnesses -- not clear to me right now (ST) what the best way to setup this up would be.
  // One suboptimal thing about how it's set up right now is that e.g. in subclass methods like
  // Box.getPN, the source.pycor gets tested once, and then if Box.getPN calls
  // Topology.getPatchNorth, then source.pycor gets redundantly tested again.
  // - JD, ST 6/3/04

}

trait XWraps extends Topology {
  override val xWraps = true

  override def wrapX(x: Double): Double =
    Topology.wrap(x, world._minPxcor - 0.5, world._maxPxcor + 0.5)

  protected def diffuseXBorder(amount: Double, vn: Int, fourWay: Boolean, scratch: Array[Array[Double]]): Unit = {
    val lastX = world.worldWidth - 1
    val lastY = world.worldHeight - 1
    val butLastCol = scratch(lastX - 1)
    val lastCol = scratch(lastX)
    val firstCol = scratch(0)
    val secondCol = scratch(1)
    var y = 1
    while (y < lastY) {
      val oldValE = scratch(lastX)(y)
      val oldValW = scratch(0)(y)
      val sumE = sum4(butLastCol(y), firstCol(y), lastCol(y+1), lastCol(y-1))
      val sumW = sum4(lastCol(y), secondCol(y), firstCol(y+1), firstCol(y-1))
      if (fourWay) {
        updatePatch(amount, vn, 4, 0, y, oldValW, sumW)
        updatePatch(amount, vn, 4, lastX, y, oldValE, sumE)
      } else {
        updatePatch(amount, vn, 8, 0, y, oldValW, sumW +
          sum4(lastCol(y+1), lastCol(y-1), secondCol(y+1), secondCol(y-1))
        )
        updatePatch(amount, vn, 8, lastX, y, oldValE, sumE +
          sum4(butLastCol(y+1), butLastCol(y-1), firstCol(y+1), firstCol(y-1))
        )
      }
      y += 1
    }
  }
}

trait XBlocks extends Topology {
  override val xWraps = false

  override def diffuseXBorder(amount: Double, vn: Int, fourWay: Boolean, scratch: Array[Array[Double]]): Unit = {
    val lastX = world.worldWidth - 1
    val lastY = world.worldHeight - 1
    val update = if (fourWay)
      (x: Int, y: Int, borderCol: Array[Double], innerCol: Array[Double]) => {
        val oldVal = borderCol(y)
        val sum = sum4(borderCol(y-1), borderCol(y+1), innerCol(y), oldVal)
        updatePatch(amount, vn, 4, x, y, oldVal, sum)
      }
    else
      (x: Int, y: Int, borderCol: Array[Double], innerCol: Array[Double]) => {
        val oldVal = borderCol(y)
        val sum =
          sum4(borderCol(y-1), borderCol(y+1), innerCol(y), oldVal) +
          sum4(innerCol(y-1), innerCol(y+1), oldVal, oldVal)
        updatePatch(amount, vn, 8, x, y, oldVal, sum)
      }
    val butLastCol = scratch(lastX - 1)
    val lastCol = scratch(lastX)
    val firstCol = scratch(0)
    val secondCol = scratch(1)
    var y = 1
    while (y < lastY) {
      update(0, y, firstCol, secondCol)
      update(lastX, y, lastCol, butLastCol)
      y += 1
    }
  }
}

trait YWraps extends Topology {
  override val yWraps = true

  override def wrapY(y: Double): Double =
    Topology.wrap(y, world._minPycor - 0.5, world._maxPycor + 0.5)

  override def diffuseYBorder(amount: Double, vn: Int, fourWay: Boolean, scratch: Array[Array[Double]]): Unit = {
    val lastX = world.worldWidth - 1
    val lastY = world.worldHeight - 1
    val butLastY = lastY - 1

    var x = 1
    while (x < lastX) {
      val e = scratch(x+1)
      val c = scratch(x)
      val w = scratch(x-1)
      val sumN = sum4(e(0), w(0), c(1), c(lastY))
      val sumS = sum4(e(lastY), w(lastY), c(0), c(butLastY))
      val oldValN = c(0)
      val oldValS = c(lastY)
      if (fourWay) {
        updatePatch(amount, vn, 4, x, 0, oldValN, sumN)
        updatePatch(amount, vn, 4, x, lastY, oldValS, sumS)
      } else {
        updatePatch(amount, vn, 8, x, 0, oldValN, sumN +
          sum4(e(1), w(1), e(lastY), w(lastY))
        )
        updatePatch(amount, vn, 8, x, lastY, oldValS, sumS +
          sum4(e(butLastY), w(butLastY), e(0), w(0))
        )
      }
      x += 1
    }
  }
}

trait YBlocks extends Topology {
  override val yWraps = false

  override def diffuseYBorder(amount: Double, vn: Int, fourWay: Boolean, scratch: Array[Array[Double]]): Unit = {
    val lastX = world.worldWidth - 1
    val lastY = world.worldHeight - 1
    val butLastY = lastY - 1

    val update = if(fourWay)
      (x: Int, y: Int, innerY: Int) => {
        val oldVal = scratch(x)(y)
        val sum = sum4(scratch(x-1)(y), scratch(x+1)(y), scratch(x)(innerY), oldVal)
        updatePatch(amount, vn, 4, x, y, oldVal, sum)
      }
    else
      (x: Int, y: Int, innerY: Int) => {
        val oldVal = scratch(x)(y)
        val sum =
          sum4(scratch(x-1)(y), scratch(x+1)(y), scratch(x)(innerY), oldVal) +
          sum4(scratch(x-1)(innerY), scratch(x+1)(innerY), oldVal, oldVal)
        updatePatch(amount, vn, 8, x, y, oldVal, sum)
      }


    var x = 1
    while (x < lastX) {
      update(x, 0, 1)
      update(x, lastY, butLastY)
      x += 1
    }
  }
}