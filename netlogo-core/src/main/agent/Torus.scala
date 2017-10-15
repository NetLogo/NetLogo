// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

@annotation.strictfp
class Torus(_world: World)
extends Topology(_world, xWraps = true, yWraps = true)
with XWraps with YWraps {

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

  override protected def diffuseCorners
  (amount: Double, vn: Int, fourWay: Boolean, scratch: Array[Array[Double]]): Unit = {
    val lastX = world.worldWidth - 1
    val lastY = world.worldHeight - 1
    val butLastY = lastY - 1
    val update = if (fourWay)
      (x: Int, y: Int, w: Array[Double], c: Array[Double], e: Array[Double], y1: Int, y2: Int) => {
        val sum = sum4(e(y), w(y), c(y1), c(y2))
        updatePatch(amount, vn, 4, x, y, c(y), sum)
      }
    else
      (x: Int, y: Int, w: Array[Double], c: Array[Double], e: Array[Double], y1: Int, y2: Int) => {
        val sum = sum4(e(y), w(y), c(y1), c(y2)) +
          sum4(e(y1), w(y1), e(y2), w(y2))
        updatePatch(amount, vn, 8, x, y, c(y), sum)
      }
    val butLastCol = scratch(lastX - 1)
    val lastCol = scratch(lastX)
    val firstCol = scratch(0)
    val secondCol = scratch(1)

    update(0,     0,     lastCol,    firstCol, secondCol, lastY,    1)
    update(0,     lastY, lastCol,    firstCol, secondCol, butLastY, 0)
    update(lastX, 0,     butLastCol, lastCol,  firstCol,  lastY,    1)
    update(lastX, lastY, butLastCol, lastCol,  firstCol,  butLastY, 0)
  }
}
