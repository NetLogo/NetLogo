// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.core.I18N
import org.nlogo.api.AgentException

// imagine a cylinder standing on end.

@annotation.strictfp
class VertCylinder(world2d: World2D)
extends Topology(world2d, xWraps = true, yWraps = false)
with XWraps with YBlocks {

  override def wrapX(x: Double): Double =
    Topology.wrap(x, world.minPxcor - 0.5, world.maxPxcor + 0.5)

  @throws(classOf[AgentException])
  override def wrapY(y: Double): Double = {
    val max = world.maxPycor + 0.5
    val min = world.minPycor - 0.5
    if (y >= max || y < min)
      throw new AgentException(I18N.errors.get("org.nlogo.agent.Topology.cantMoveTurtleBeyondWorldEdge"))
    y
  }

  override def distanceWrap(dx: Double, dy: Double, x1: Double, y1: Double, x2: Double, y2: Double): Double = {
    val dx2 = world.worldWidth - StrictMath.abs(x1 - x2)
    val dxMin =
      if (StrictMath.abs(dx2) < StrictMath.abs(dx))
        dx2
      else
        dx
    world.rootsTable.gridRoot(dxMin * dxMin + dy * dy)
  }

  override def towardsWrap(headingX: Double, headingY: Double): Double = {
    val headingX2 = Topology.wrap(headingX,
      world.worldWidth / -2.0,
      world.worldWidth / 2.0)
    if (headingY == 0)
      if (headingX2 > 0) 90 else 270
    else if (headingX2 == 0)
      if (headingY > 0) 0 else 180
    else
      ((270 + StrictMath.toDegrees(StrictMath.PI + StrictMath.atan2(-headingY, headingX2)))
        % 360)
  }

  override def shortestPathX(x1: Double, x2: Double): Double = {
    val xprime =
      if (x1 > x2)
        x1 + (world.worldWidth - StrictMath.abs(x1 - x2))
      else
        x1 - (world.worldWidth - StrictMath.abs(x1 - x2))
    if (StrictMath.abs(x2 - x1) > StrictMath.abs(xprime - x1))
      xprime
    else if (x1 > x2)
      x1 - StrictMath.abs(x1 - x2)
    else
      x1 + StrictMath.abs(x1 - x2)
  }

  override def shortestPathY(y1: Double, y2: Double) = if (y1 > y2) y1 - StrictMath.abs(y1 - y2) else y1 + StrictMath.abs(y1 - y2)

  override def followOffsetY = 0.0

  override def getPN(source: Patch): Patch =
    if (source.pycor == world.maxPycor)
      null
    else
      world.fastGetPatchAt(source.pxcor, source.pycor + 1)

  override def getPE(source: Patch): Patch =
    world.fastGetPatchAt(
      if (source.pxcor == world.maxPxcor)
        world.minPxcor
      else
        source.pxcor + 1,
      source.pycor)

  override def getPS(source: Patch): Patch =
    if (source.pycor == world.minPycor)
      null
    else
      world.fastGetPatchAt(source.pxcor, source.pycor - 1)

  override def getPW(source: Patch): Patch =
    world.fastGetPatchAt(
      if (source.pxcor == world.minPxcor)
        world.maxPxcor
      else
        source.pxcor - 1,
      source.pycor)

  override def getPNE(source: Patch): Patch =
    if (source.pycor == world.maxPycor)
      null
    else
      world.fastGetPatchAt(
        if (source.pxcor == world.maxPxcor)
          world.minPxcor
        else
          source.pxcor + 1,
        source.pycor + 1)

  override def getPSE(source: Patch): Patch =
    if (source.pycor == world.minPycor)
      null
    else
      world.fastGetPatchAt(
        if (source.pxcor == world.maxPxcor)
          world.minPxcor
        else
          source.pxcor + 1,
        source.pycor - 1)

  override def getPSW(source: Patch): Patch =
    if (source.pycor == world.minPycor)
      null
    else
      world.fastGetPatchAt(
        if (source.pxcor == world.minPxcor)
          world.maxPxcor
        else
          source.pxcor - 1,
        source.pycor - 1)

  override def getPNW(source: Patch): Patch =
    if (source.pycor == world.maxPycor)
      null
    else
      world.fastGetPatchAt(
        if (source.pxcor == world.minPxcor)
          world.maxPxcor
        else
          source.pxcor - 1,
        source.pycor + 1)

  override protected def diffuseCorners
  (amount: Double, vn: Int, fourWay: Boolean, scratch: Array[Array[Double]]): Unit = {
    val lastX = world.worldWidth - 1
    val butLastX = lastX - 1
    val lastY = world.worldHeight - 1
    val butLastY = lastY - 1
    val update = if (fourWay)
      (x: Int, y: Int, innerX: Int, innerY: Int, wrappedX: Int) => {
        val oldVal = scratch(x)(y)
        updatePatch(amount, vn, 4, x, y, oldVal,
          sum4(scratch(innerX)(y), scratch(x)(innerY), scratch(wrappedX)(y), oldVal))
      }
    else
      (x: Int, y: Int, innerX: Int, innerY: Int, wrappedX: Int) => {
        val oldVal = scratch(x)(y)
        updatePatch(amount, vn, 8, x, y, oldVal,
          sum4(scratch(innerX)(y), scratch(x)(innerY), scratch(wrappedX)(y), oldVal) +
            sum4(scratch(innerX)(innerY), scratch(wrappedX)(innerY), oldVal, oldVal)
        )
      }
    update(0, 0, 1, 1, lastX)
    update(0, lastY, 1, butLastY, lastX)
    update(lastX, 0, butLastX, 1, 0)
    update(lastX, lastY, butLastX, butLastY, 0)
  }
}
