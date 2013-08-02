// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api.{ AgentException, AgentKind, I18N }

@annotation.strictfp
class Box(_world: World)
extends Topology(_world, xWraps = false, yWraps = false) {

  @throws(classOf[AgentException])
  override def wrapX(x: Double): Double  = {
    val max = world.maxPxcor + 0.5
    val min = world.minPxcor - 0.5
    if (x >= max || x < min)
      throw new AgentException(
        I18N.errors.get(
          "org.nlogo.agent.Box.cantMoveTurtleBeyondWorldEdge"))
    x
  }

  @throws(classOf[AgentException])
  override def wrapY(y: Double): Double = {
    val max = world.maxPycor + 0.5
    val min = world.minPycor - 0.5
    if (y >= max || y < min)
      throw new AgentException(
        I18N.errors.get(
          "org.nlogo.agent.Box.cantMoveTurtleBeyondWorldEdge"))
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

  override def shortestPathX(x1: Double, x2: Double) = x2
  override def shortestPathY(y1: Double, y2: Double) = y2
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

  @throws(classOf[AgentException])
  @throws(classOf[PatchException])
  override def diffuse(amount: Double, vn: Int) {
    val xx = world.worldWidth
    val yy = world.worldHeight
    val scratch = world.getPatchScratch
    val scratch2 = Array.ofDim[Double](xx, yy)
    val minx = world.minPxcor
    val miny = world.minPycor
    var x, y = 0

    try while(y < yy) {
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
        world.fastGetPatchAt(wrapX(x).toInt, wrapY(y).toInt)) }

    y = 0
    while (y < yy) {
      x = 0
      while (x < xx) {
        val diffuseVal = (scratch(x)(y) / 8) * amount
        if (y > 0 && y < yy - 1 && x > 0 && x < xx - 1) {
          scratch2(x    )(y    ) += scratch(x)(y) - (8 * diffuseVal)
          scratch2(x - 1)(y - 1) += diffuseVal
          scratch2(x - 1)(y    ) += diffuseVal
          scratch2(x - 1)(y + 1) += diffuseVal
          scratch2(x    )(y + 1) += diffuseVal
          scratch2(x    )(y - 1) += diffuseVal
          scratch2(x + 1)(y - 1) += diffuseVal
          scratch2(x + 1)(y    ) += diffuseVal
          scratch2(x + 1)(y + 1) += diffuseVal
        }
        else if (y > 0 && y < yy - 1)
          if (x == 0) {
            scratch2(x    )(y    ) += scratch(x)(y) - (5 * diffuseVal)
            scratch2(x    )(y + 1) += diffuseVal
            scratch2(x    )(y - 1) += diffuseVal
            scratch2(x + 1)(y - 1) += diffuseVal
            scratch2(x + 1)(y    ) += diffuseVal
            scratch2(x + 1)(y + 1) += diffuseVal
          } else {
            scratch2(x    )(y    ) += scratch(x)(y) - (5 * diffuseVal)
            scratch2(x    )(y + 1) += diffuseVal
            scratch2(x    )(y - 1) += diffuseVal
            scratch2(x - 1)(y - 1) += diffuseVal
            scratch2(x - 1)(y    ) += diffuseVal
            scratch2(x - 1)(y + 1) += diffuseVal
          }
        else if (x > 0 && x < xx - 1)
          if (y == 0) {
            scratch2(x    )(y    ) += scratch(x)(y) - (5 * diffuseVal)
            scratch2(x - 1)(y    ) += diffuseVal
            scratch2(x - 1)(y + 1) += diffuseVal
            scratch2(x    )(y + 1) += diffuseVal
            scratch2(x + 1)(y    ) += diffuseVal
            scratch2(x + 1)(y + 1) += diffuseVal
          } else {
            scratch2(x    )(y    ) += scratch(x)(y) - (5 * diffuseVal)
            scratch2(x - 1)(y    ) += diffuseVal
            scratch2(x - 1)(y - 1) += diffuseVal
            scratch2(x    )(y - 1) += diffuseVal
            scratch2(x + 1)(y    ) += diffuseVal
            scratch2(x + 1)(y - 1) += diffuseVal
          }
        else if (x == 0)
          if (y == 0) {
            scratch2(x    )(y    ) += scratch(x)(y) - (3 * diffuseVal)
            scratch2(x    )(y + 1) += diffuseVal
            scratch2(x + 1)(y    ) += diffuseVal
            scratch2(x + 1)(y + 1) += diffuseVal
          } else {
            scratch2(x    )(y    ) += scratch(x)(y) - (3 * diffuseVal)
            scratch2(x    )(y - 1) += diffuseVal
            scratch2(x + 1)(y    ) += diffuseVal
            scratch2(x + 1)(y - 1) += diffuseVal
          }
        else if (y == 0) {
          scratch2(x    )(y    ) += scratch(x)(y) - (3 * diffuseVal)
          scratch2(x    )(y + 1) += diffuseVal
          scratch2(x - 1)(y    ) += diffuseVal
          scratch2(x - 1)(y + 1) += diffuseVal
        }
        else {
          scratch2(x    )(y    ) += scratch(x)(y) - (3 * diffuseVal)
          scratch2(x    )(y - 1) += diffuseVal
          scratch2(x - 1)(y    ) += diffuseVal
          scratch2(x - 1)(y - 1) += diffuseVal
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

    y = 0
    while (y < yy) {
      x = 0
      while (x < xx) {
        val diffuseVal = (scratch(x)(y) / 4) * amount
        if (y > 0 && y < yy - 1 && x > 0 && x < xx - 1) {
          scratch2(x    )(y    ) += scratch(x)(y) - (4 * diffuseVal)
          scratch2(x - 1)(y    ) += diffuseVal
          scratch2(x    )(y + 1) += diffuseVal
          scratch2(x    )(y - 1) += diffuseVal
          scratch2(x + 1)(y    ) += diffuseVal
        }
        else if (y > 0 && y < yy - 1)
          if (x == 0) {
            scratch2(x    )(y    ) += scratch(x)(y) - (3 * diffuseVal)
            scratch2(x    )(y + 1) += diffuseVal
            scratch2(x    )(y - 1) += diffuseVal
            scratch2(x + 1)(y    ) += diffuseVal
          } else {
            scratch2(x    )(y    ) += scratch(x)(y) - (3 * diffuseVal)
            scratch2(x    )(y + 1) += diffuseVal
            scratch2(x    )(y - 1) += diffuseVal
            scratch2(x - 1)(y    ) += diffuseVal
          }
        else if (x > 0 && x < xx - 1)
          if (y == 0) {
            scratch2(x    )(y    ) += scratch(x)(y) - (3 * diffuseVal)
            scratch2(x - 1)(y    ) += diffuseVal
            scratch2(x    )(y + 1) += diffuseVal
            scratch2(x + 1)(y    ) += diffuseVal
          } else {
            scratch2(x    )(y    ) += scratch(x)(y) - (3 * diffuseVal)
            scratch2(x - 1)(y    ) += diffuseVal
            scratch2(x    )(y - 1) += diffuseVal
            scratch2(x + 1)(y    ) += diffuseVal
          }
        else if (x == 0)
          if (y == 0) {
            scratch2(x    )(y    ) += scratch(x)(y) - (2 * diffuseVal)
            scratch2(x    )(y + 1) += diffuseVal
            scratch2(x + 1)(y    ) += diffuseVal
          } else {
            scratch2(x    )(y    ) += scratch(x)(y) - (2 * diffuseVal)
            scratch2(x    )(y - 1) += diffuseVal
            scratch2(x + 1)(y    ) += diffuseVal
          }
        else if (y == 0) {
          scratch2(x    )(y    ) += scratch(x)(y) - (2 * diffuseVal)
          scratch2(x    )(y + 1) += diffuseVal
          scratch2(x - 1)(y    ) += diffuseVal
        }
        else {
          scratch2(x    )(y    ) += scratch(x)(y) - (2 * diffuseVal)
          scratch2(x    )(y - 1) += diffuseVal
          scratch2(x - 1)(y    ) += diffuseVal
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

}
