// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api.AgentException

@annotation.strictfp
class Torus(_world: World)
extends Topology(_world, xWraps = true, yWraps = true) {

  override def wrapX(x: Double): Double =
    Topology.wrap(x, world._minPxcor - 0.5, world._maxPxcor + 0.5)

  override def wrapY(y: Double): Double =
    Topology.wrap(y, world._minPycor - 0.5, world._maxPycor + 0.5)

  override def distanceWrap(dx: Double, dy: Double, x1: Double, y1: Double, x2: Double, y2: Double): Double = {
    val dx2 = world.worldWidth - StrictMath.abs(x1 - x2)
    val dxMin =
      if (StrictMath.abs(dx2) < StrictMath.abs(dx))
        dx2
      else
        dx
    val dy2 = world.worldHeight - StrictMath.abs(y1 - y2)
    val dyMin =
      if (StrictMath.abs(dy2) < StrictMath.abs(dy))
        dy2
      else
        dy
    world.rootsTable.gridRoot(dxMin * dxMin + dyMin * dyMin)
  }

  override def towardsWrap(headingX: Double, headingY: Double): Double = {
    val headingX2 =
      Topology.wrap(headingX,
        world.worldWidth / -2.0,
        world.worldWidth / 2.0)
    val headingY2 =
      Topology.wrap(headingY,
        world.worldHeight / -2.0,
        world.worldHeight / 2.0)
    if (headingY2 == 0)
      if (headingX2 > 0) 90 else 270
    else if (headingX2 == 0)
      if (headingY2 > 0) 0 else 180
    else
      ((270 + StrictMath.toDegrees(StrictMath.PI + StrictMath.atan2(-headingY2, headingX2)))
        % 360)
  }

  override def getPN(source: Patch): Patch =
    world.fastGetPatchAt(
      source.pxcor,
      if (source.pycor == world._maxPycor)
        world._minPycor
      else
        source.pycor + 1)
  override def getPE(source: Patch): Patch =
    world.fastGetPatchAt(
      if (source.pxcor == world._maxPxcor)
        world._minPxcor
      else
        source.pxcor + 1,
      source.pycor)
  override def getPS(source: Patch): Patch =
    world.fastGetPatchAt(
      source.pxcor,
      if (source.pycor == world._minPycor)
        world._maxPycor
      else
        source.pycor - 1)
  override def getPW(source: Patch): Patch =
    world.fastGetPatchAt(
      if (source.pxcor == world._minPxcor)
        world._maxPxcor
      else
        source.pxcor - 1,
      source.pycor)
  override def getPNE(source: Patch): Patch =
    world.fastGetPatchAt(
      if (source.pxcor == world._maxPxcor)
        world._minPxcor
      else
        source.pxcor + 1,
      if (source.pycor == world._maxPycor)
        world._minPycor
      else
        source.pycor + 1)
  override def getPSE(source: Patch): Patch =
    world.fastGetPatchAt(
      if (source.pxcor == world._maxPxcor)
        world._minPxcor
      else
        source.pxcor + 1,
      if (source.pycor == world._minPycor)
        world._maxPycor
      else
        source.pycor - 1)
  override def getPSW(source: Patch): Patch =
    world.fastGetPatchAt(
      if (source.pxcor == world._minPxcor)
        world._maxPxcor
      else
        source.pxcor - 1,
      if (source.pycor == world._minPycor)
        world._maxPycor
      else
        source.pycor - 1)
  override def getPNW(source: Patch): Patch =
    world.fastGetPatchAt(
      if (source.pxcor == world._minPxcor)
        world._maxPxcor
      else
        source.pxcor - 1,
      if (source.pycor == world._maxPycor)
        world._minPycor
      else
        source.pycor + 1)

  override def shortestPathX(x1: Double, x2: Double): Double = {
    val xprime =
      if (x1 > x2)
        x1 + (world.worldWidth - StrictMath.abs(x1 - x2))
      else
        x1 - (world.worldWidth - StrictMath.abs(x1 - x2))
    if (StrictMath.abs(x2 - x1) > StrictMath.abs(xprime - x1))
      xprime
    else
      if (x1 > x2)
        x1 - StrictMath.abs(x1 - x2)
      else
        x1 + StrictMath.abs(x1 - x2)
  }

  override def shortestPathY(y1: Double, y2: Double): Double = {
    val yprime =
      if (y1 > y2)
        y1 + (world.worldHeight - StrictMath.abs(y1 - y2)) * 1
      else
        y1 + (world.worldHeight - StrictMath.abs(y1 - y2)) * -1
    if (StrictMath.abs(y2 - y1) > StrictMath.abs(yprime - y1))
      yprime
    else
      if (y1 > y2)
        y1 - StrictMath.abs(y1 - y2)
      else
        y1 + StrictMath.abs(y1 - y2)
  }

  @throws(classOf[AgentException])
  @throws(classOf[PatchException])
  override def diffuse(amount: Double, vn: Int) {
    val scratch = getPatchScratch(vn)
    val xx = world.worldWidth
    val yy = world.worldHeight
    val a: Array[Double] = Array.ofDim(8)
    var y = 0
    while (y < yy) {
      var x = 0
      while (x < xx) {
        val xe = (x + xx - 1) % xx
        val xw = (x + 1) % xx
        val yn = (y + 1) % yy
        val ys = (y + yy - 1) % yy
        a(0) = scratch(xe)(ys)
        insert(scratch(xe)(y ), a, 1)
        insert(scratch(xe)(yn), a, 2)
        insert(scratch(x )(ys), a, 3)
        insert(scratch(x )(yn), a, 4)
        insert(scratch(xw)(ys), a, 5)
        insert(scratch(xw)(y ), a, 6)
        insert(scratch(xw)(yn), a, 7)
        val sum = a(0) + a(1) + a(2) + a(3) + a(4) + a(5) + a(6) + a(7)
        val oldval = scratch(x)(y)
        val newval = oldval + amount * (sum / 8 - oldval)
        if (newval != oldval) {
          world.patches.getByIndex(y * xx + x).setPatchVariable(vn, Double.box(newval))
        }
        x += 1
      }
      y += 1
    }
  }

  @throws(classOf[AgentException])
  @throws(classOf[PatchException])
  override def diffuse4(amount: Double, vn: Int) {
    val scratch = getPatchScratch(vn)
    val xx = world.worldWidth
    val yy = world.worldHeight
    val a: Array[Double] = Array.ofDim(4)
    var y = 0
    while (y < yy) {
      var x = 0
      while (x < xx) {
        a(0) = scratch((x + xx - 1) % xx)((y         )     )
        insert(scratch((x         )     )((y + 1     ) % yy), a, 1)
        insert(scratch((x + 1     ) % xx)((y         )     ), a, 2)
        insert(scratch((x         )     )((y + yy - 1) % yy), a, 3)
        val sum = a(0) + a(1) + a(2) + a(3)
        val oldval = scratch(x)(y)
        val newval = oldval + amount * (sum / 4 - oldval)
        if (newval != oldval)
          world.patches.getByIndex(xx * y + x).setPatchVariable(vn, Double.box(newval))
        x += 1
      }
      y += 1
    }
  }


  /*
  def sortedSum(vals: Array[Double]) = {
    var result: Double = 0
    var end = vals.length
    var minIx = 0
    var minVal = vals(minIx)
    while (end > 1) {
      var i = minIx + 1
      while (i < end) {
        val v = vals(i)
        if (v < minVal) {
          minIx += 1
          val t = vals(minIx)
          vals(minIx) = v
          vals(i) = t
          minVal = v
          minIx = i
        }
        i+=1
      }
      result += minVal
      vals(minIx) = vals(end - 1)
      if (minIx > 0)
        minIx -= 1
      minVal = vals(minIx)
      end -= 1
    }
    result + vals(0)
  }
  */

  def insert(x: Double, vals: Array[Double], end: Int): Unit = {
    var i = end
    while (i > 0 && x < vals(i - 1)) {
      vals(i) = vals(i - 1)
      i -= 1
    }
    vals(i) = x
  }

  def kahanSum(vals: Array[Double]) = {
    var sum = 0.0
    var c = 0.0
    var i = 0
    while (i < vals.length) {
      val y = vals(i) - c
      val t = sum + y
      c = (t - sum) - y
      sum = t
      i+=1
    }
    sum
  }

}
