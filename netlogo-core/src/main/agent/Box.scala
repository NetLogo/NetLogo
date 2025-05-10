// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api.AgentException
import org.nlogo.core.I18N

class Box(world2d: World2D) extends Topology(world2d) with XBlocks with YBlocks {
  @throws(classOf[AgentException])
  override def wrapX(x: Double): Double  = {
    val max = world.maxPxcor + 0.5
    val min = world.minPxcor - 0.5
    if (x >= max || x < min)
      throw new AgentException(
        I18N.errors.get(
          "org.nlogo.agent.Topology.cantMoveTurtleBeyondWorldEdge"))
    x
  }

  @throws(classOf[AgentException])
  override def wrapY(y: Double): Double = {
    val max = world.maxPycor + 0.5
    val min = world.minPycor - 0.5
    if (y >= max || y < min)
      throw new AgentException(
        I18N.errors.get(
          "org.nlogo.agent.Topology.cantMoveTurtleBeyondWorldEdge"))
    y
  }

  override def distanceWrap(dx: Double, dy: Double, x1: Double, y1: Double, x2: Double, y2: Double): Double =
    world.rootsTable.gridRoot(dx * dx + dy * dy)

  override def towardsWrap(headingX: Double, headingY: Double): Double =
    if (headingX == 0)
      if (headingY > 0) 0 else 180
    else if (headingY == 0)
      if (headingX > 0) 90 else 270
    else
      ((270 + StrictMath.toDegrees (StrictMath.PI + StrictMath.atan2(-headingY, headingX)))
        % 360)

  // These are odd in order to match how tortoise calculates shortest paths and maintain floating point equality
  // Suffice it to say that while it is true that x2 == x1 - abs(x1 - x2) when x1 > x2, that is not computationaly true when
  // considering floating point operations.  FD 11/14/13
  override def shortestPathX(x1: Double, x2: Double) = if (x1 > x2) x1 - StrictMath.abs(x1 - x2) else x1 + StrictMath.abs(x1 - x2)
  override def shortestPathY(y1: Double, y2: Double) = if (y1 > y2) y1 - StrictMath.abs(y1 - y2) else y1 + StrictMath.abs(y1 - y2)
  override def followOffsetX = 0.0
  override def followOffsetY = 0.0

  override def getPN(source: Patch): Patch =
    if (source.pycor == world.maxPycor)
      null
    else
      world.fastGetPatchAt(source.pxcor, source.pycor + 1)
  override def getPE(source: Patch): Patch =
    if (source.pxcor == world.maxPxcor)
      null
    else
      world.fastGetPatchAt(source.pxcor + 1, source.pycor)
  override def getPS(source: Patch): Patch =
    if (source.pycor == world.minPycor)
      null
    else
      world.fastGetPatchAt(source.pxcor, source.pycor - 1)
  override def getPW(source: Patch): Patch =
    if (source.pxcor == world.minPxcor)
      null
    else
      world.fastGetPatchAt(source.pxcor - 1, source.pycor)
  override def getPNE(source: Patch): Patch =
    if (source.pxcor == world.maxPxcor || source.pycor == world.maxPycor)
      null
    else
      world.fastGetPatchAt(source.pxcor + 1, source.pycor + 1)
  override def getPSE(source: Patch): Patch =
    if (source.pxcor == world.maxPxcor || source.pycor == world.minPycor)
      null
    else
      world.fastGetPatchAt(source.pxcor + 1, source.pycor - 1)
  override def getPSW(source: Patch): Patch =
    if (source.pxcor == world.minPxcor || source.pycor == world.minPycor)
      null
    else
      world.fastGetPatchAt(source.pxcor - 1, source.pycor - 1)
  override def getPNW(source: Patch): Patch =
    if (source.pxcor == world.minPxcor || source.pycor == world.maxPycor)
      null
    else
      world.fastGetPatchAt(source.pxcor - 1, source.pycor + 1)

  override protected def diffuseCorners(amount: Double,
                                        vn: Int,
                                        fourWay: Boolean,
                                        scratch: Array[Array[Double]]): Unit = {
    val ww = world.worldWidth
    val wh = world.worldHeight
    val lastX = ww - 1
    val secondLastX = (lastX - 1) % ww
    val secondX = 1 % ww
    val lastY = wh - 1
    val secondLastY = (lastY - 1) % wh
    val secondY = 1 % wh
    val update = if (fourWay)
      (x: Int, y: Int, innerX: Int, innerY: Int) => {
        val oldVal = scratch(x)(y)
        updatePatch(amount, vn, 4, x, y, oldVal,
          sum4(scratch(innerX)(y), scratch(x)(innerY), oldVal, oldVal))
      }
    else
      (x: Int, y: Int, innerX: Int, innerY: Int) => {
        val oldVal = scratch(x)(y)
        updatePatch(amount, vn, 8, x, y, oldVal,
          sum4(scratch(innerX)(y), scratch(x)(innerY), oldVal, oldVal) +
          sum4(scratch(innerX)(innerY), oldVal, oldVal, oldVal)
        )
      }
    update(0,     0,     secondX,     secondY)
    update(0,     lastY, secondX,     secondLastY)
    update(lastX, 0,     secondLastX, secondY)
    update(lastX, lastY, secondLastX, secondLastY)
  }
}
