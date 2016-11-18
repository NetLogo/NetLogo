// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.core.AgentKind
import org.nlogo.api.AgentException

class Torus(_world: World) extends Topology(_world) {
  import Topology.wrap
  //wrapping coordinates

  override def wrapX(x: Double): Double =
    wrap(x, world.minPxcor - 0.5, world.maxPxcor + 0.5)

  override def wrapY(y: Double): Double =
    wrap(y, world.minPycor - 0.5, world.maxPycor + 0.5)

  override def distanceWrap(dx: Double, dy: Double, x1: Double, y1: Double, x2: Double, y2: Double): Double = {
    val dx2 =
      if (x1 > x2)
        (x2 + world.worldWidth) - x1
      else
        (x2 - world.worldWidth) - x1

    val newDx = if (StrictMath.abs(dx2) < StrictMath.abs(dx)) dx2 else dx

    val dy2 =
      if (y1 > y2)
        (y2 + world.worldHeight) - y1
      else
        (y2 - world.worldHeight) - y1
    val newDy = if (StrictMath.abs(dy2) < StrictMath.abs(dy)) dy2 else dy

    return world.rootsTable.gridRoot(newDx * newDx + newDy * newDy)
  }

  override def towardsWrap(headingX: Double, headingY: Double): Double = {
    val newHeadingX =
      wrap(headingX, (- world.worldWidth.asInstanceOf[Double] / 2.0), (world.worldWidth / 2.0))

    val newHeadingY =
      wrap(headingY, (- world.worldHeight.asInstanceOf[Double] / 2.0), (world.worldHeight / 2.0))

    if (newHeadingY == 0) {
      if (newHeadingX > 0) 90 else 270
    } else if (newHeadingX == 0) {
      if (newHeadingY > 0) 0 else 180
    } else
      (270 + StrictMath.toDegrees(StrictMath.PI + StrictMath.atan2(-newHeadingY, newHeadingX))) % 360
  }

  @throws(classOf[AgentException])
  override def getPatchAt(xc: Double, yc: Double): Patch =
    world.getPatchAt(xc, yc)

  override def getNeighbors(source: Patch): IndexedAgentSet = {
    val neighbors =
      if (source.pxcor == world.maxPxcor && source.pxcor == world.minPxcor) {
        if (source.pycor == world.maxPycor && source.pycor == world.minPycor) {
          Array[Agent]()
        } else {
          Array[Agent](getPatchNorth(source), getPatchSouth(source))
        }
      } else if (source.pycor == world.maxPycor && source.pycor == world.minPycor) {
        Array[Agent](getPatchEast(source), getPatchWest(source))
      } else {
        Array[Agent](getPatchNorth(source), getPatchEast(source),
          getPatchSouth(source), getPatchWest(source),
          getPatchNorthEast(source), getPatchSouthEast(source),
          getPatchSouthWest(source), getPatchNorthWest(source))
      }

    AgentSet.fromArray(AgentKind.Patch, neighbors.distinct)
  }

  override def getNeighbors4(source: Patch): IndexedAgentSet = {
    val neighbors =
      if (source.pxcor == world.maxPxcor && source.pxcor == world.minPxcor) {
        if (source.pycor == world.maxPycor && source.pycor == world.minPycor)
          Array[Agent]()
        else
          Array[Agent](getPatchNorth(source), getPatchSouth(source))
      } else if (source.pycor == world.maxPycor && source.pycor == world.minPycor) {
        Array[Agent](getPatchEast(source), getPatchWest(source))
      } else {
        Array[Agent](getPatchNorth(source), getPatchEast(source), getPatchSouth(source), getPatchWest(source))
      }

    AgentSet.fromArray(AgentKind.Patch, neighbors.distinct)
  }

  override def shortestPathX(x1: Double, x2: Double): Double = {
    val xprime =
      if (x1 > x2) x2 + world.worldWidth
      else x2 - world.worldWidth

    if (StrictMath.abs(x2 - x1) > StrictMath.abs(xprime - x1))
      xprime
    else
      x2
  }

  override def shortestPathY(y1: Double, y2: Double): Double = {
    val yprime =
      if (y1 > y2) y2 + world.worldHeight
      else y2 - world.worldHeight

    if (StrictMath.abs(y2 - y1) > StrictMath.abs(yprime - y1))
      yprime
    else
      y2
  }

  @throws(classOf[AgentException])
  @throws(classOf[PatchException])
  override def diffuse(diffuseparam: Double, vn: Int): Unit = {
    val xx = world.worldWidth
    val yy = world.worldHeight
    val scratch = world.getPatchScratch

    for {
      y <- 0 until yy
      x <- 0 until xx
    } {
      try {
        scratch(x)(y) =
          world.fastGetPatchAt(wrapX(x).asInstanceOf[Int], wrapY(y).asInstanceOf[Int])
            .getPatchVariable(vn)
            .asInstanceOf[Double]
            .doubleValue
      } catch {
        case ex: ClassCastException =>
          throw new PatchException(world.fastGetPatchAt(wrapX(x).asInstanceOf[Int], wrapY(y).asInstanceOf[Int]))
      }
    }

    for {
      y <- yy until yy * 2
      x <- xx until xx * 2
    } {
      var sum = scratch((x - 1) % xx)((y - 1) % yy)
      sum += scratch((x - 1) % xx)((y) % yy)
      sum += scratch((x - 1) % xx)((y + 1) % yy)
      sum += scratch((x) % xx)((y - 1) % yy)
      sum += scratch((x) % xx)((y + 1) % yy)
      sum += scratch((x + 1) % xx)((y - 1) % yy)
      sum += scratch((x + 1) % xx)((y) % yy)
      sum += scratch((x + 1) % xx)((y + 1) % yy)

      val oldval = scratch(x - xx)(y - yy)
      val newval = oldval * (1.0 - diffuseparam) + (sum / 8) * diffuseparam

      if (newval != oldval)
        world.getPatchAt(x - xx, y - yy).setPatchVariable(vn, Double.box(newval))
    }
  }

  @throws(classOf[AgentException])
  @throws(classOf[PatchException])
  override def diffuse4(diffuseparam: Double, vn: Int): Unit = {
    val xx = world.worldWidth
    val yy = world.worldHeight
    val scratch = world.getPatchScratch

    for {
      y <- 0 until yy
      x <- 0 until xx
    } {
      try {
        scratch(x)(y) =
          world.fastGetPatchAt(wrapX(x).asInstanceOf[Int], wrapY(y).asInstanceOf[Int])
            .getPatchVariable(vn)
            .asInstanceOf[Double]
            .doubleValue
      } catch {
        case ex: ClassCastException =>
        throw new PatchException(world.fastGetPatchAt(wrapX(x).asInstanceOf[Int], wrapY(y).asInstanceOf[Int]))
      }
    }

    for {
      y <- 0 until yy
      x <- 0 until xx
    } {
        var sum = 0.0d
        sum += scratch((x + xx - 1) % xx)((y + yy) % yy)  // left patch
        sum += scratch((x + xx) % xx)((y + yy + 1) % yy)  // top patch
        sum += scratch((x + xx + 1) % xx)((y + yy) % yy)  // right patch
        sum += scratch((x + xx) % xx)((y + yy - 1) % yy)  // bottom patch

        val newval = scratch(x)(y) * (1 - diffuseparam) + sum * diffuseparam / 4
        if (newval != scratch(x)(y))
          world.getPatchAt(x, y).setPatchVariable(vn, Double.box(newval))
    }
  }

  //get patch

  override def getPN(source: Patch): Patch =
    getPatchNorth(source)

  override def getPE(source: Patch): Patch =
    getPatchEast(source)

  override def getPS(source: Patch): Patch =
    getPatchSouth(source)

  override def getPW(source: Patch): Patch =
    getPatchWest(source)

  override def getPNE(source: Patch): Patch =
    getPatchNorthEast(source)

  override def getPSE(source: Patch): Patch =
    getPatchSouthEast(source)

  override def getPSW(source: Patch): Patch =
    getPatchSouthWest(source)

  override def getPNW(source: Patch): Patch =
    getPatchNorthWest(source)
}
