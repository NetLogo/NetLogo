// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.core.I18N
import org.nlogo.api.AgentException

// imagine a cylinder lying on its side

@annotation.strictfp
class HorizCylinder(world2d: World2D)
extends Topology(world2d, xWraps = false, yWraps = true)
with XBlocks with YWraps{

  @throws(classOf[AgentException])
  override def wrapX(x: Double): Double = {
    val max = world.maxPxcor + 0.5
    val min = world.minPxcor - 0.5
    if (x >= max || x < min)
      throw new AgentException(I18N.errors.get("org.nlogo.agent.Topology.cantMoveTurtleBeyondWorldEdge"))
    x
  }

  override def wrapY(y: Double): Double =
    Topology.wrap(y, world.minPycor - 0.5, world.maxPycor + 0.5)

  override def distanceWrap(dx: Double, dy: Double, x1: Double, y1: Double, x2: Double, y2: Double): Double = {
    val dy2 = world.worldHeight - StrictMath.abs(y1 - y2)
    val dyMin =
      if (StrictMath.abs(dy2) < StrictMath.abs(dy))
        dy2
      else
        dy
    world.rootsTable.gridRoot(dx * dx + dyMin * dyMin)
  }

  override def towardsWrap(headingX: Double, headingY: Double): Double = {
    val headingY2 = Topology.wrap(
      headingY,
      world.worldHeight / -2.0,
      world.worldHeight / 2.0)
    if (headingY2 == 0)
      if (headingX > 0) 90 else 270
    else if (headingX == 0)
      if (headingY2 > 0) 0 else 180
    else ((270 + StrictMath.toDegrees (StrictMath.PI + StrictMath.atan2(-headingY2, headingX)))
      % 360)
  }

  override def shortestPathX(x1: Double, x2: Double) = if (x1 > x2) x1 - StrictMath.abs(x1 - x2) else x1 + StrictMath.abs(x1 - x2)

  override def shortestPathY(y1: Double, y2: Double) = {
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

  override def followOffsetX = 0.0

  override def getPN(source: Patch): Patch =
    world.fastGetPatchAt(
      source.pxcor,
      if (source.pycor == world.maxPycor)
        world.minPycor
      else
        source.pycor + 1)
  override def getPE(source: Patch): Patch =
    if (source.pxcor == world.maxPxcor)
      null
    else
      world.fastGetPatchAt(source.pxcor + 1, source.pycor)
  override def getPS(source: Patch): Patch =
    world.fastGetPatchAt(
      source.pxcor,
      if (source.pycor == world.minPycor)
        world.maxPycor
      else
        source.pycor - 1)
  override def getPW(source: Patch): Patch =
    if (source.pxcor == world.minPxcor)
      null
    else
      world.fastGetPatchAt(source.pxcor - 1, source.pycor)
  override def getPNE(source: Patch): Patch =
    if (source.pxcor == world.maxPxcor)
      null
    else
      world.fastGetPatchAt(source.pxcor + 1,
        if (source.pycor == world.maxPycor)
          world.minPycor
        else
          source.pycor + 1)
  override def getPSE(source: Patch): Patch =
    if (source.pxcor == world.maxPxcor)
      null
    else
      world.fastGetPatchAt(source.pxcor + 1,
        if (source.pycor == world.minPycor)
          world.maxPycor
        else
          source.pycor - 1)
  override def getPSW(source: Patch): Patch =
    if (source.pxcor == world.minPxcor)
      null
    else
      world.fastGetPatchAt(source.pxcor - 1,
        if (source.pycor == world.minPycor)
          world.maxPycor
        else
          source.pycor - 1)
  override def getPNW(source: Patch): Patch =
    if (source.pxcor == world.minPxcor)
      null
    else
      world.fastGetPatchAt(source.pxcor - 1,
        if (source.pycor == world.maxPycor)
          world.minPycor
        else
          source.pycor + 1)

  override protected def diffuseCorners
  (amount: Double, vn: Int, fourWay: Boolean, scratch: Array[Array[Double]]): Unit = {
    val lastX = world.worldWidth - 1
    val butLastX = lastX - 1
    val lastY = world.worldHeight - 1
    val butLastY = lastY - 1
    val update = if (fourWay)
      (x: Int, y: Int, innerX: Int, innerY: Int, wrappedY: Int) => {
        val oldVal = scratch(x)(y)
        updatePatch(amount, vn, 4, x, y, oldVal,
          sum4(scratch(innerX)(y), scratch(x)(innerY), scratch(x)(wrappedY), oldVal))
      }
    else
      (x: Int, y: Int, innerX: Int, innerY: Int, wrappedY: Int) => {
        val oldVal = scratch(x)(y)
        updatePatch(amount, vn, 8, x, y, oldVal,
          sum4(scratch(innerX)(y), scratch(x)(innerY), scratch(x)(wrappedY), oldVal) +
          sum4(scratch(innerX)(innerY), scratch(innerX)(wrappedY), oldVal, oldVal)
        )
      }
    update(0, 0, 1, 1, lastY)
    update(0, lastY, 1, butLastY, 0)
    update(lastX, 0, butLastX, 1, lastY)
    update(lastX, lastY, butLastX, butLastY, 0)
  }
}
