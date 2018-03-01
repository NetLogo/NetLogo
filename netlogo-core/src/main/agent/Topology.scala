// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api, api.AgentException
import java.util.ArrayList

@annotation.strictfp
object Topology {

  // factory method
  def get(world: World2D, xWraps: Boolean, yWraps: Boolean): Topology =
    (xWraps, yWraps) match {
      case (true, true) => new Torus(world)
      case (true, false) => new VertCylinder(world)
      case (false, true) => new HorizCylinder(world)
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
    diffuseCenter(amount, vn, fourWay = false, scratch)
    diffuseXBorder(amount, vn, fourWay = false, scratch)
    diffuseYBorder(amount, vn, fourWay = false, scratch)
    diffuseCorners(amount, vn, fourWay = false, scratch)
  }

  @throws(classOf[AgentException])
  @throws(classOf[PatchException])
  def diffuse4(amount: Double, vn: Int): Unit = {
    val scratch = getPatchScratch(vn)
    diffuseCenter(amount, vn, fourWay = true, scratch)
    diffuseXBorder(amount, vn, fourWay = true, scratch)
    diffuseYBorder(amount, vn, fourWay = true, scratch)
    diffuseCorners(amount, vn, fourWay = true, scratch)
  }

  protected def diffuseCenter(amount: Double, vn: Int, fourWay: Boolean, scratch: Array[Array[Double]]): Unit = {
    val lastX = world.worldWidth - 1
    val lastY = world.worldHeight - 1
    var x = 1
    while (x < lastX) {
      val e = scratch(x + 1)
      val c = scratch(x)
      val w = scratch(x - 1)
      var y = 1
      while (y < lastY) {
        val oldVal = c(y)
        val sum = sum4(e(y), w(y), c(y - 1), c(y + 1))
        if (fourWay)
          updatePatch(amount, vn, 4, x, y, oldVal, sum)
        else
          updatePatch(amount, vn, 8, x, y, oldVal,
            sum + sum4(e(y + 1), e(y - 1), w(y + 1), w(y - 1)))
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
  @inline
  final protected def sum4(a: Double, b: Double, c: Double, d: Double): Double = {
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

  @inline
  final protected def updatePatch(amount: Double, vn: Int, directions: Int,
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


  // getRegion retrieves indices of the (2 * R + 1) by (2 * R + 1) square/region of patches centered
  // at X,Y in order first from left to right, then top to bottom. In order to account for wrapping,
  // there are four main cases in both the horizontal and vertical axes. getRegion handles the y axis,
  // and calls getRegionRow to handle the x axis. Having the indices in this particular order is
  // important. It is the order that the patches are actually stored in the underlying array and
  // allows the rather fast System.arraycopy Java function to actually retrieve the patches.
  // in InRadiusOrCone.scala.
  //
  // 4 cases:
  // 1. x - r >= 0 and x + r <= w - 1 (fully within the world)
  // 2. x - r < 0 and x + r >= w      (wraps in both directions)
  // 3. x - r < 0                     (wraps below 0)
  // 4. x + r >= w                    (wraps above w - 1)
  //
  // getRegion's output is a list of integer tuples. Each tuple represents a range of continuous
  // world.patches indices to be used in InRadiusOrCone.scala with System.arraycopy. In each tuple,
  // the first int is the first index in the range, and the last index is the (last index + 1). For
  // example, if there is a range of indices from 2 to 9 (inclusive), then this is the tuple (2, 10).
  //
  // EH 2/11/2018

  def getRegion(initialX: Int, initialY: Int, initialR: Int): ArrayList[(Int, Int)] = {

    // translate from Netlogo coordinates to array indices
    val x: Int = initialX - world.minPxcor
    val y: Int = world.worldHeight - 1 - (initialY - world.minPycor)
    val r: Int = initialR

    val ans: ArrayList[(Int, Int)] = new ArrayList()

    val low_within = y - r >= 0
    val high_within = y + r <= world.worldHeight - 1

    val y_ranges = {
      if (low_within && high_within) { // completely within world
        Array((y - r, y + r + 1))

      } else if (!low_within && !high_within) { // wider than both sides of the world
        Array((0, world.worldHeight))

      } else if (low_within) { // wider on low side
        if (yWraps) {
          Array((0, y + r - world.worldHeight + 1), (y - r, world.worldHeight))
        } else {
          Array((y - r, world.worldHeight))
        }

      } else { // wider on high side
        if (yWraps) {
          Array((0, y + r + 1), (world.worldHeight + y - r, world.worldHeight))
        } else {
          Array((0, y + r + 1))
        }
      }
    }

    var i = y_ranges(0)._1
    while (i < y_ranges(0)._2) {
      getRegionRow(x, r, i * world.worldWidth, ans)
      i += 1
    }

    if (y_ranges.length > 1) {
      i = y_ranges(1)._1
      while (i < y_ranges(1)._2) {
        getRegionRow(x, r, i * world.worldWidth, ans)
        i += 1
      }
    }

    ans
  }

  // helper for getRegion
  @scala.inline
  private final def getRegionRow(x: Int, r: Int, offset: Int, arr: ArrayList[(Int, Int)]): Unit = {
    // similar logic as second half of getRegion

    val low_within = x - r >= 0
    val high_within = x + r <= world.worldWidth - 1

    if (low_within && high_within) {
      mergeAdd((offset + x - r, offset + x + r + 1), arr)

    } else if (!low_within && !high_within) {
      mergeAdd((offset + 0, offset + world.worldWidth), arr)

    } else if (!low_within) {
      mergeAdd((offset + 0, offset + x + r + 1), arr)
      if (xWraps) {
        mergeAdd((offset + world.worldWidth + x - r, offset + world.worldWidth), arr)
      }

    } else { // !high_within
      if (xWraps) {
        mergeAdd((offset + 0, offset + x + r - world.worldWidth + 1), arr)
      }
      mergeAdd((offset + x - r, offset + world.worldWidth), arr)
    }

  }

  // helper fo getRegion/getRegionRow
  // combines pairs and merges them when they intersect
  @scala.inline
  private final def mergeAdd(value: (Int, Int), arr: ArrayList[(Int, Int)]): Unit = {
    val s = arr.size()
    if (s == 0 || arr.get(s - 1)._2 < value._1) {
      arr.add(value)
    } else {
      val last = arr.get(s - 1)
      arr.set(s - 1, (last._1.min(value._1), value._2.max(last._2)))
    }
  }

}

trait XWraps extends Topology {
  override val xWraps = true

  override def wrapX(x: Double): Double =
    Topology.wrap(x, world._minPxcor - 0.5, world._maxPxcor + 0.5)

  protected def diffuseXBorder(amount: Double, vn: Int, fourWay: Boolean, scratch: Array[Array[Double]]): Unit = {
    val ww = world.worldWidth
    val wh = world.worldHeight
    val lastX = (ww - 1) % ww
    val lastY = (wh - 1) % wh
    val secondLastCol = scratch((lastX - 1) % ww)
    val lastCol = scratch(lastX)
    val firstCol = scratch(0)
    val secondCol = scratch(1 % ww)
    var y = 1
    while (y < lastY) {
      val oldValE = scratch(lastX)(y)
      val oldValW = scratch(0)(y)
      val sumE = sum4(secondLastCol(y), firstCol(y), lastCol(y + 1), lastCol(y - 1))
      val sumW = sum4(lastCol(y), secondCol(y), firstCol(y + 1), firstCol(y - 1))
      if (fourWay) {
        updatePatch(amount, vn, 4, 0, y, oldValW, sumW)
        updatePatch(amount, vn, 4, lastX, y, oldValE, sumE)
      } else {
        updatePatch(amount, vn, 8, 0, y, oldValW, sumW +
          sum4(lastCol(y + 1), lastCol(y - 1), secondCol(y + 1), secondCol(y - 1))
        )
        updatePatch(amount, vn, 8, lastX, y, oldValE, sumE +
          sum4(secondLastCol(y + 1), secondLastCol(y - 1), firstCol(y + 1), firstCol(y - 1))
        )
      }
      y += 1
    }
  }
}

trait XBlocks extends Topology {
  override val xWraps = false

  override def diffuseXBorder(amount: Double, vn: Int, fourWay: Boolean, scratch: Array[Array[Double]]): Unit = {
    val ww = world.worldWidth
    val wh = world.worldHeight
    val lastX = ww - 1
    val lastY = wh - 1
    val update = if (fourWay)
      (x: Int, y: Int, borderCol: Array[Double], innerCol: Array[Double]) => {
        val oldVal = borderCol(y)
        val sum = sum4(borderCol(y - 1), borderCol(y + 1), innerCol(y), oldVal)
        updatePatch(amount, vn, 4, x, y, oldVal, sum)
      }
    else
      (x: Int, y: Int, borderCol: Array[Double], innerCol: Array[Double]) => {
        val oldVal = borderCol(y)
        val sum =
          sum4(borderCol(y - 1), borderCol(y + 1), innerCol(y), oldVal) +
            sum4(innerCol(y - 1), innerCol(y + 1), oldVal, oldVal)
        updatePatch(amount, vn, 8, x, y, oldVal, sum)
      }
    val secondLastCol = scratch((lastX - 1) % ww)
    val lastCol = scratch(lastX)
    val firstCol = scratch(0)
    val secondCol = scratch(1 % ww)
    var y = 1
    while (y < lastY) {
      update(0, y, firstCol, secondCol)
      update(lastX, y, lastCol, secondLastCol)
      y += 1
    }
  }
}

trait YWraps extends Topology {
  override val yWraps = true

  override def wrapY(y: Double): Double =
    Topology.wrap(y, world._minPycor - 0.5, world._maxPycor + 0.5)

  override def diffuseYBorder(amount: Double, vn: Int, fourWay: Boolean, scratch: Array[Array[Double]]): Unit = {
    val ww = world.worldWidth
    val wh = world.worldHeight
    val lastX = ww - 1
    val lastY = wh - 1
    val secondLastY = (lastY - 1) % wh
    val secondY = 1 % wh

    var x = 1
    while (x < lastX) {
      val e = scratch(x + 1)
      val c = scratch(x)
      val w = scratch(x - 1)
      val sumN = sum4(e(0), w(0), c(secondY), c(lastY))
      val sumS = sum4(e(lastY), w(lastY), c(0), c(secondLastY))
      val oldValN = c(0)
      val oldValS = c(lastY)
      if (fourWay) {
        updatePatch(amount, vn, 4, x, 0, oldValN, sumN)
        updatePatch(amount, vn, 4, x, lastY, oldValS, sumS)
      } else {
        updatePatch(amount, vn, 8, x, 0, oldValN, sumN +
          sum4(e(secondY), w(secondY), e(lastY), w(lastY))
        )
        updatePatch(amount, vn, 8, x, lastY, oldValS, sumS +
          sum4(e(secondLastY), w(secondLastY), e(0), w(0))
        )
      }
      x += 1
    }
  }
}

trait YBlocks extends Topology {
  override val yWraps = false

  override def diffuseYBorder(amount: Double, vn: Int, fourWay: Boolean, scratch: Array[Array[Double]]): Unit = {
    val ww = world.worldWidth
    val wh = world.worldHeight
    val lastX = ww - 1
    val lastY = wh - 1
    val secondLastY = (lastY - 1) % wh
    val secondY = 1 % wh

    val update = if (fourWay)
      (x: Int, y: Int, innerY: Int) => {
        val oldVal = scratch(x)(y)
        val sum = sum4(scratch(x - 1)(y), scratch(x + 1)(y), scratch(x)(innerY), oldVal)
        updatePatch(amount, vn, 4, x, y, oldVal, sum)
      }
    else
      (x: Int, y: Int, innerY: Int) => {
        val oldVal = scratch(x)(y)
        val sum =
          sum4(scratch(x - 1)(y), scratch(x + 1)(y), scratch(x)(innerY), oldVal) +
            sum4(scratch(x - 1)(innerY), scratch(x + 1)(innerY), oldVal, oldVal)
        updatePatch(amount, vn, 8, x, y, oldVal, sum)
      }


    var x = 1
    while (x < lastX) {
      update(x, 0, secondY)
      update(x, lastY, secondLastY)
      x += 1
    }
  }
}
