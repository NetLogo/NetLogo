// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api.{ AgentException, AgentKind }

@annotation.strictfp
class Torus(_world: World)
extends Topology(_world, xWraps = true, yWraps = true) {

  override def wrapX(x: Double): Double =
    Topology.wrap(x, world.minPxcor - 0.5, world.maxPxcor + 0.5)

  override def wrapY(y: Double): Double =
    Topology.wrap(y, world.minPycor - 0.5, world.maxPycor + 0.5)

  override def distanceWrap(dx: Double, dy: Double, x1: Double, y1: Double, x2: Double, y2: Double): Double = {
    val dx2 =
      if (x1 > x2)
        (x2 + world.worldWidth) - x1
      else
        (x2 - world.worldWidth) - x1
    val dxMin =
      if (StrictMath.abs(dx2) < StrictMath.abs(dx))
        dx2
      else
        dx
    val dy2 =
      if (y1 > y2)
        (y2 + world.worldHeight) - y1
      else
        (y2 - world.worldHeight) - y1
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
      if (source.pycor == world.maxPycor)
        world.minPycor
      else
        source.pycor + 1)
  override def getPE(source: Patch): Patch =
    world.fastGetPatchAt(
      if (source.pxcor == world.maxPxcor)
        world.minPxcor
      else
        source.pxcor + 1,
      source.pycor)
  override def getPS(source: Patch): Patch =
    world.fastGetPatchAt(
      source.pxcor,
      if (source.pycor == world.minPycor)
        world.maxPycor
      else
        source.pycor - 1)
  override def getPW(source: Patch): Patch =
    world.fastGetPatchAt(
      if (source.pxcor == world.minPxcor)
        world.maxPxcor
      else
        source.pxcor - 1,
      source.pycor)
  override def getPNE(source: Patch): Patch =
    world.fastGetPatchAt(
      if (source.pxcor == world.maxPxcor)
        world.minPxcor
      else
        source.pxcor + 1,
      if (source.pycor == world.maxPycor)
        world.minPycor
      else
        source.pycor + 1)
  override def getPSE(source: Patch): Patch =
    world.fastGetPatchAt(
      if (source.pxcor == world.maxPxcor)
        world.minPxcor
      else
        source.pxcor + 1,
      if (source.pycor == world.minPycor)
        world.maxPycor
      else
        source.pycor - 1)
  override def getPSW(source: Patch): Patch =
    world.fastGetPatchAt(
      if (source.pxcor == world.minPxcor)
        world.maxPxcor
      else
        source.pxcor - 1,
      if (source.pycor == world.minPycor)
        world.maxPycor
      else
        source.pycor - 1)
  override def getPNW(source: Patch): Patch =
    world.fastGetPatchAt(
      if (source.pxcor == world.minPxcor)
        world.maxPxcor
      else
        source.pxcor - 1,
      if (source.pycor == world.maxPycor)
        world.minPycor
      else
        source.pycor + 1)

  override def shortestPathX(x1: Double, x2: Double): Double = {
    val xprime =
      if (x1 > x2)
        x2 + world.worldWidth
      else
        x2 - world.worldWidth
    if (StrictMath.abs(x2 - x1) > StrictMath.abs(xprime - x1))
      xprime
    else
      x2
  }

  override def shortestPathY(y1: Double, y2: Double): Double = {
    val yprime =
      if (y1 > y2)
        y2 + world.worldHeight
      else
        y2 - world.worldHeight
    if (StrictMath.abs(y2 - y1) > StrictMath.abs(yprime - y1))
      yprime
    else
      y2
  }

  @throws(classOf[AgentException])
  @throws(classOf[PatchException])
  override def diffuse(amount: Double, vn: Int) {
    val xx = world.worldWidth
    val xx2 = xx * 2
    val yy = world.worldHeight
    val yy2 = yy * 2
    val scratch = world.getPatchScratch
    var x, y = 0
    try while(y < yy) {
      x = 0
      while (x < xx) {
        scratch(x)(y) =
          world.fastGetPatchAt(wrapX(x).toInt, wrapY(y).toInt)
            .getPatchVariable(vn).asInstanceOf[java.lang.Double].doubleValue
        x += 1
      }
      y += 1
    } catch { case _: ClassCastException =>
      throw new PatchException(
        world.fastGetPatchAt(wrapX(x).toInt, wrapY(y).toInt))
    }
    y = yy
    while (y < yy2) {
      x = xx
      while (x < xx2) {
        val sum =
          scratch((x - 1) % xx)((y - 1) % yy) +
          scratch((x - 1) % xx)((y    ) % yy) +
          scratch((x - 1) % xx)((y + 1) % yy) +
          scratch((x    ) % xx)((y - 1) % yy) +
          scratch((x    ) % xx)((y + 1) % yy) +
          scratch((x + 1) % xx)((y - 1) % yy) +
          scratch((x + 1) % xx)((y    ) % yy) +
          scratch((x + 1) % xx)((y + 1) % yy)
        val oldval = scratch(x - xx)(y - yy)
        val newval = oldval * (1.0 - amount) + (sum / 8) * amount
        if (newval != oldval)
          world.getPatchAt(x - xx, y - yy)
            .setPatchVariable(vn, Double.box(newval))
        x += 1
      }
      y += 1
    }
  }

  @throws(classOf[AgentException])
  @throws(classOf[PatchException])
  override def diffuse4(amount: Double, vn: Int) {
    val xx = world.worldWidth
    val yy = world.worldHeight
    val scratch = world.getPatchScratch
    var x, y = 0
    try while(y < yy) {
      x = 0
      while(x < xx) {
        scratch(x)(y) =
          world.fastGetPatchAt(wrapX(x).toInt, wrapY(y).toInt)
            .getPatchVariable(vn)
            .asInstanceOf[java.lang.Double].doubleValue
        x += 1
      }
      y += 1
    }
    catch { case _: ClassCastException =>
      throw new PatchException(
        world.fastGetPatchAt(wrapX(x).toInt, wrapY(y).toInt))
    }
    y = 0
    while (y < yy) {
      x = 0
      while (x < xx) {
        val sum =
          scratch((x + xx - 1) % xx)((y + yy    ) % yy) +
          scratch((x + xx    ) % xx)((y + yy + 1) % yy) +
          scratch((x + xx + 1) % xx)((y + yy    ) % yy) +
          scratch((x + xx    ) % xx)((y + yy - 1) % yy)
        val newval = scratch(x)(y) * (1 - amount) + sum * amount / 4
        if (newval != scratch(x)(y))
          world.getPatchAt(x, y).setPatchVariable(vn, Double.box(newval))
        x += 1
      }
      y += 1
    }
  }

}
