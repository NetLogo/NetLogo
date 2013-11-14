// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api.{ AgentException, AgentKind }

// imagine a cylinder standing on end.

@annotation.strictfp
class VertCylinder(_world: World)
extends Topology(_world, xWraps = true, yWraps = false) {

  override def wrapX(x: Double): Double =
    Topology.wrap(x, world.minPxcor - 0.5, world.maxPxcor + 0.5)

  @throws(classOf[AgentException])
  override def wrapY(y: Double): Double = {
    val max = world.maxPycor + 0.5
    val min = world.minPycor - 0.5
    if (y >= max || y < min)
      throw new AgentException("Cannot move turtle beyond the world's edge.")
    y
  }

  override def distanceWrap(dx: Double, dy: Double, x1: Double, y1: Double, x2: Double, y2: Double): Double = {
    val dx2 =
      if (x1 > x2)
        x2 + world.worldWidth - x1
      else
        x2 - world.worldWidth - x1
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
        x2 + world.worldWidth
      else
        x2 - world.worldWidth
    if (StrictMath.abs(x2 - x1) > StrictMath.abs(xprime - x1))
      xprime
    else
      x2
  }

  override def shortestPathY(y1: Double, y2: Double) = y2

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

  @throws(classOf[AgentException])
  @throws(classOf[PatchException])
  override def diffuse(amount: Double, vn: Int) {
    val xx = world.worldWidth
    val yy = world.worldHeight
    val xx2 = xx * 2
    val yy2 = yy * 2
    val scratch = world.getPatchScratch
    val scratch2 = Array.ofDim[Double](xx, yy)
    val minx = world.minPxcor
    val miny = world.minPycor
    var x, y = 0

    try while(y < yy) {
      x = 0
      while (x < xx ) {
        scratch(x)(y) =
          world.fastGetPatchAt(x + minx, y + miny)
            .getPatchVariable(vn)
            .asInstanceOf[java.lang.Double].doubleValue
        scratch2(x)(y) = 0
        x += 1
      }
      y += 1
    }
    catch { case _: ClassCastException =>
      throw new PatchException(
        world.fastGetPatchAt(wrapX(x).toInt, wrapY(y).toInt))
    }

    y = yy
    while(y < yy2) {
      x = xx
      while (x < xx2) {
        val diffuseVal = (scratch(x - xx)(y - yy) / 8) * amount
        if (y > yy && y < yy2 - 1) {
          scratch2(x - xx)(y - yy) += scratch(x - xx)(y - yy) - (8 * diffuseVal)
          scratch2((x - 1) % xx)((y - 1) % yy) += diffuseVal
          scratch2((x - 1) % xx)(y % yy) += diffuseVal
          scratch2((x - 1) % xx)((y + 1) % yy) += diffuseVal
          scratch2(x % xx)((y + 1) % yy) += diffuseVal
          scratch2(x % xx)((y - 1) % yy) += diffuseVal
          scratch2((x + 1) % xx)((y - 1) % yy) += diffuseVal
          scratch2((x + 1) % xx)(y % yy) += diffuseVal
          scratch2((x + 1) % xx)((y + 1) % yy) += diffuseVal
        } else if (y == yy) {
          scratch2(x - xx)(y - yy) += scratch(x - xx)(y - yy) - (5 * diffuseVal)
          scratch2((x - 1) % xx)(y % yy) += diffuseVal
          scratch2((x - 1) % xx)((y + 1) % yy) += diffuseVal
          scratch2(x % xx)((y + 1) % yy) += diffuseVal
          scratch2((x + 1) % xx)(y % yy) += diffuseVal
          scratch2((x + 1) % xx)((y + 1) % yy) += diffuseVal
        } else {
          scratch2(x - xx)(y - yy) += scratch(x - xx)(y - yy) - (5 * diffuseVal)
          scratch2((x - 1) % xx)(y % yy) += diffuseVal
          scratch2((x - 1) % xx)((y - 1) % yy) += diffuseVal
          scratch2(x % xx)((y - 1) % yy) += diffuseVal
          scratch2((x + 1) % xx)(y % yy) += diffuseVal
          scratch2((x + 1) % xx)((y - 1) % yy) += diffuseVal
        }
        x += 1
      }
      y += 1
    }
    y = 0
    while (y < yy) {
      x = 0
      while (x < xx) {
        if (scratch2(x)(y) != scratch(x)(y))
          world.getPatchAtWrap(x + minx, y + miny)
              .setPatchVariable(vn, Double.box(scratch2(x)(y)))
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
    val xx2 = xx * 2
    val yy2 = yy * 2
    val scratch = world.getPatchScratch
    val scratch2 = Array.ofDim[Double](xx, yy)
    val minx = world.minPxcor
    val miny = world.minPycor
    var x, y = 0
    try while (y < yy) {
      x = 0
      while (x < xx) {
        scratch(x)(y) =
          world.fastGetPatchAt(x + minx, y + miny)
            .getPatchVariable(vn)
            .asInstanceOf[java.lang.Double].doubleValue
        scratch2(x)(y) = 0
        x += 1
      }
      y += 1
    }
    catch { case _: ClassCastException =>
      throw new PatchException(
        world.fastGetPatchAt(wrapX(x).toInt, wrapY(y).toInt))
    }
    y = yy
    while (y < yy2) {
      x = xx
      while (x < xx2) {
        val diffuseVal = (scratch(x - xx)(y - yy) / 4) * amount
        if (y > yy && y < yy2 - 1) {
          scratch2(x - xx)(y - yy) += scratch(x - xx)(y - yy) - (4 * diffuseVal)
          scratch2((x - 1) % xx)(y % yy) += diffuseVal
          scratch2(x % xx)((y + 1) % yy) += diffuseVal
          scratch2(x % xx)((y - 1) % yy) += diffuseVal
          scratch2((x + 1) % xx)(y % yy) += diffuseVal
        } else if (y == yy) {
          scratch2(x - xx)(y - yy) += scratch(x - xx)(y - yy) - (3 * diffuseVal)
          scratch2((x - 1) % xx)(y % yy) += diffuseVal
          scratch2(x % xx)((y + 1) % yy) += diffuseVal
          scratch2((x + 1) % xx)(y % yy) += diffuseVal
        } else {
          scratch2(x - xx)(y - yy) += scratch(x - xx)(y - yy) - (3 * diffuseVal)
          scratch2((x - 1) % xx)(y % yy) += diffuseVal
          scratch2(x % xx)((y - 1) % yy) += diffuseVal
          scratch2((x + 1) % xx)(y % yy) += diffuseVal
        }
        x += 1
      }
      y += 1
    }
    y = 0
    while (y < yy) {
      x = 0
      while(x < xx) {
        if (scratch2(x)(y) != scratch(x)(y))
          world.getPatchAtWrap(x + minx, y + miny)
            .setPatchVariable(vn, Double.box(scratch2(x)(y)))
        x += 1
      }
      y += 1
    }
  }

}
