// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api.{ AgentException, AgentKind, I18N }

@annotation.strictfp
class Box(_world: World) extends Topology(_world) {

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

  @throws(classOf[AgentException])
  override def getPatchAt(xc: Double, yc: Double): Patch =
    if (yc >= world.maxPycor + 0.5 ||
        yc <  world.minPycor - 0.5 ||
        xc >= world.maxPxcor + 0.5 ||
        xc <  world.minPxcor - 0.5)
      null
    else
      world.getPatchAt(xc, yc)

  override def shortestPathX(x1: Double, x2: Double) = x2
  override def shortestPathY(y1: Double, y2: Double) = y2
  override def followOffsetX = 0.0
  override def followOffsetY = 0.0

  override def getNeighbors(source: Patch): AgentSet = {
    val xLoc = source.pxcor
    val yLoc = source.pycor
    // special case: only one patch in world
    if (xLoc == world.maxPxcor && xLoc == world.minPxcor &&
        yLoc == world.maxPycor && yLoc == world.minPycor)
      world.noPatches
    else if (xLoc == world.maxPxcor)
      if (xLoc == world.minPxcor)
        if (yLoc == world.maxPycor)
          AgentSet.fromArray(AgentKind.Patch, Array[Agent](
            getPatchSouth(source)))
        else if (yLoc == world.minPycor)
          AgentSet.fromArray(AgentKind.Patch, Array[Agent](
            getPatchNorth(source)))
        else
          AgentSet.fromArray(AgentKind.Patch, Array[Agent](
            getPatchNorth(source),
            getPatchSouth(source)))
      else if (yLoc == world.maxPycor)
        if (yLoc == world.minPycor)
          AgentSet.fromArray(AgentKind.Patch, Array[Agent](
            getPatchWest(source)))
        else
          AgentSet.fromArray(AgentKind.Patch, Array[Agent](
            getPatchSouth(source),
            getPatchWest(source),
            getPatchSouthWest(source)))
      else if (yLoc == world.minPycor)
        AgentSet.fromArray(AgentKind.Patch, Array[Agent](
          getPatchNorth(source),
          getPatchWest(source),
          getPatchNorthWest(source)))
      else
        AgentSet.fromArray(AgentKind.Patch, Array[Agent](
          getPatchNorth(source),
          getPatchSouth(source),
          getPatchWest(source),
          getPatchSouthWest(source),
          getPatchNorthWest(source)))
    else if (xLoc == world.minPxcor)
      if (yLoc == world.maxPycor)
        if (yLoc == world.minPycor)
          AgentSet.fromArray(AgentKind.Patch, Array[Agent](
            getPatchEast(source)))
        else
          AgentSet.fromArray(AgentKind.Patch, Array[Agent](
            getPatchEast(source),
            getPatchSouth(source),
            getPatchSouthEast(source)))
      else if (yLoc == world.minPycor)
        AgentSet.fromArray(AgentKind.Patch, Array[Agent](
          getPatchNorth(source),
          getPatchEast(source),
          getPatchNorthEast(source)))
      else
        AgentSet.fromArray(AgentKind.Patch, Array[Agent](
          getPatchNorth(source),
          getPatchEast(source),
          getPatchSouth(source),
          getPatchNorthEast(source),
          getPatchSouthEast(source)))
    else if (yLoc == world.maxPycor)
      if (yLoc == world.minPycor)
        AgentSet.fromArray(AgentKind.Patch, Array[Agent](
          getPatchEast(source),
          getPatchWest(source)))
      else
        AgentSet.fromArray(AgentKind.Patch, Array[Agent](
          getPatchEast(source),
          getPatchSouth(source),
          getPatchWest(source),
          getPatchSouthEast(source),
          getPatchSouthWest(source)))
    else if (yLoc == world.minPycor)
      AgentSet.fromArray(AgentKind.Patch, Array[Agent](
        getPatchNorth(source),
        getPatchEast(source),
        getPatchWest(source),
        getPatchNorthEast(source),
        getPatchNorthWest(source)))
    else
      AgentSet.fromArray(AgentKind.Patch, Array[Agent](
        getPatchNorth(source),
        getPatchEast(source),
        getPatchSouth(source),
        getPatchWest(source),
        getPatchNorthEast(source),
        getPatchSouthEast(source),
        getPatchSouthWest(source),
        getPatchNorthWest(source)))
  }

  override def getNeighbors4(source: Patch): AgentSet = {
    val xLoc = source.pxcor
    val yLoc = source.pycor
    if (xLoc == world.maxPxcor)
      if (xLoc == world.minPxcor)
        if (yLoc == world.maxPycor)
          if (yLoc == world.minPycor)
            world.noPatches
          else
            AgentSet.fromArray(AgentKind.Patch, Array[Agent](
              getPatchSouth(source)))
        else if (yLoc == world.minPycor)
          AgentSet.fromArray(AgentKind.Patch, Array[Agent](
            getPatchNorth(source)))
        else
          AgentSet.fromArray(AgentKind.Patch, Array[Agent](
            getPatchNorth(source),
            getPatchSouth(source)))
      else if (yLoc == world.maxPycor)
        if (yLoc == world.minPycor)
          AgentSet.fromArray(AgentKind.Patch, Array[Agent](
            getPatchWest(source)))
        else
          AgentSet.fromArray(AgentKind.Patch, Array[Agent](
            getPatchSouth(source),
            getPatchWest(source)))
      else if (yLoc == world.minPycor)
        AgentSet.fromArray(AgentKind.Patch, Array[Agent](
          getPatchNorth(source),
          getPatchWest(source)))
      else
        AgentSet.fromArray(AgentKind.Patch, Array[Agent](
          getPatchNorth(source),
          getPatchSouth(source),
          getPatchWest(source)))
    else if (xLoc == world.minPxcor)
      if (yLoc == world.maxPycor)
        if (yLoc == world.minPycor)
          AgentSet.fromArray(AgentKind.Patch, Array[Agent](
            getPatchEast(source)))
        else
          AgentSet.fromArray(AgentKind.Patch, Array[Agent](
            getPatchEast(source),
            getPatchSouth(source)))
      else if (yLoc == world.minPycor)
        AgentSet.fromArray(AgentKind.Patch, Array[Agent](
          getPatchNorth(source),
          getPatchEast(source)))
      else
        AgentSet.fromArray(AgentKind.Patch, Array[Agent](
          getPatchNorth(source),
          getPatchEast(source),
          getPatchSouth(source)))
    else if (yLoc == world.maxPycor)
      if (yLoc == world.minPycor)
        AgentSet.fromArray(AgentKind.Patch, Array[Agent](
          getPatchEast(source),
          getPatchWest(source)))
      else
        AgentSet.fromArray(AgentKind.Patch, Array[Agent](
          getPatchEast(source),
          getPatchSouth(source),
          getPatchWest(source)))
    else if (yLoc == world.minPycor)
      AgentSet.fromArray(AgentKind.Patch, Array[Agent](
        getPatchNorth(source),
        getPatchEast(source),
        getPatchWest(source)))
    else
      AgentSet.fromArray(AgentKind.Patch, Array[Agent](
        getPatchNorth(source),
        getPatchEast(source),
        getPatchSouth(source),
        getPatchWest(source)))
  }

  override def getPN(source: Patch): Patch =
    if (source.pycor == world.maxPycor)
      null
    else
      getPatchNorth(source)

  override def getPE(source: Patch): Patch =
    if (source.pxcor == world.maxPxcor)
      null
    else
      getPatchEast(source)

  override def getPS(source: Patch): Patch =
    if (source.pycor == world.minPycor)
      null
    else
      getPatchSouth(source)

  override def getPW(source: Patch): Patch =
    if (source.pxcor == world.minPxcor)
      null
    else
      getPatchWest(source)

  override def getPNE(source: Patch): Patch =
    if (source.pxcor == world.maxPxcor || source.pycor == world.maxPycor)
      null
    else
      getPatchNorthEast(source)

  override def getPSE(source: Patch): Patch =
    if (source.pxcor == world.maxPxcor || source.pycor == world.minPycor)
      null
    else
      getPatchSouthEast(source)

  override def getPSW(source: Patch): Patch =
    if (source.pxcor == world.minPxcor || source.pycor == world.minPycor)
      null
    else
      getPatchSouthWest(source)

  override def getPNW(source: Patch): Patch =
    if (source.pxcor == world.minPxcor || source.pycor == world.maxPycor)
      null
    else
      getPatchNorthWest(source)

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
