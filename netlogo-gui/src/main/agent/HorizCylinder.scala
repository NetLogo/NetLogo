// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.core.AgentKind
import org.nlogo.api.AgentException

//world wraps along y-axis but not x-axis
class HorizCylinder(world: World) extends Topology(world) {
  import Topology.wrap

  @throws(classOf[AgentException])
  override def wrapX(x: Double): Double = {
    val max = world.maxPxcor + 0.5
    val min = world.minPxcor - 0.5
    if (x >= max || x < min) {
      throw new AgentException("Cannot move turtle beyond the world's edge.")
    }
    x
  }

  override def wrapY(y: Double): Double = {
    wrap(y, world.minPycor - 0.5, world.maxPycor + 0.5)
  }

  override def distanceWrap(dx: Double, dy: Double, x1: Double, y1: Double, x2: Double, y2: Double): Double = {
    val dy2 =
      if (y1 > y2)
        (y2 + world.worldHeight) - y1
      else
        (y2 - world.worldHeight) - y1

    val computedDy =
      if (StrictMath.abs(dy2) < StrictMath.abs(dy)) dy2 else dy

    return world.rootsTable.gridRoot(dx * dx + computedDy * computedDy)
  }

  override def towardsWrap(headingX: Double, headingY: Double): Double = {
    val newHeadingY = wrap(headingY, (- world.worldHeight.asInstanceOf[Double] / 2.0), (world.worldHeight / 2.0))

    if (newHeadingY == 0)
      if (headingX > 0) 90 else 270
    if (headingX == 0)
      if (newHeadingY > 0) 0 else 180

    (270 + StrictMath.toDegrees(StrictMath.PI + StrictMath.atan2(-newHeadingY, headingX))) % 360
  }

  @throws(classOf[AgentException])
  override def getPatchAt(xc: Double, yc: Double): Patch = {
    if ((xc > world.maxPxcor + 0.5) || (xc < world.minPxcor - 0.5))
      null
    else
      world.getPatchAt(xc, yc)
  }

  override def shortestPathX(x1: Double, x2: Double): Double = x2

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
  override def diffuse(diffuseparam: Double, vn: Int): Unit = {
    val xx = world.worldWidth
    val yy = world.worldHeight
    val scratch: Array[Array[Double]] = world.getPatchScratch
    val scratch2: Array[Array[Double]] = Array.fill[Array[Double]](xx)(Array.fill(yy)(0.0d))
    val minx = world.minPxcor
    val miny = world.minPycor

    for {
      y <- 0 until yy
      x <- 0 until xx
      } {
        try {
          scratch(x)(y) =
            (world.fastGetPatchAt(x + minx, y + miny).getPatchVariable(vn).asInstanceOf[java.lang.Double]).doubleValue
            scratch2(x)(y) = 0
          } catch {
            case ex: ClassCastException =>
              throw new PatchException(world.fastGetPatchAt(wrapX(x).asInstanceOf[Int], wrapY(y).asInstanceOf[Int]))
          }
      }

    for {
      y <- yy until yy * 2
      x <- xx until xx * 2
    } {
      val diffuseVal = (scratch(x - xx)(y - yy) / 8) * diffuseparam

      if (x > xx && x < ((xx * 2) - 1)) {
        scratch2(x - xx)(y - yy) += scratch(x - xx)(y - yy) - (8 * diffuseVal)
        scratch2((x - 1) % xx)((y - 1) % yy) += diffuseVal
        scratch2((x - 1) % xx)(y % yy) += diffuseVal
        scratch2((x - 1) % xx)((y + 1) % yy) += diffuseVal
        scratch2(x % xx)((y + 1) % yy) += diffuseVal
        scratch2(x % xx)((y - 1) % yy) += diffuseVal
        scratch2((x + 1) % xx)((y - 1) % yy) += diffuseVal
        scratch2((x + 1) % xx)(y % yy) += diffuseVal
        scratch2((x + 1) % xx)((y + 1) % yy) += diffuseVal
      } else if (x == xx) {
        scratch2(x - xx)(y - yy) += scratch(x - xx)(y - yy) - (5 * diffuseVal)
        scratch2(x % xx)((y + 1) % yy) += diffuseVal
        scratch2(x % xx)((y - 1) % yy) += diffuseVal
        scratch2((x + 1) % xx)((y - 1) % yy) += diffuseVal
        scratch2((x + 1) % xx)(y % yy) += diffuseVal
        scratch2((x + 1) % xx)((y + 1) % yy) += diffuseVal
      } else {
        scratch2(x - xx)(y - yy) += scratch(x - xx)(y - yy) - (5 * diffuseVal)
        scratch2(x % xx)((y + 1) % yy) += diffuseVal
        scratch2(x % xx)((y - 1) % yy) += diffuseVal
        scratch2((x - 1) % xx)((y - 1) % yy) += diffuseVal
        scratch2((x - 1) % xx)(y % yy) += diffuseVal
        scratch2((x - 1) % xx)((y + 1) % yy) += diffuseVal
        }
    }

    for {
      y <- 0 until yy
      x <- 0 until xx
    } {
      if (scratch2(x)(y) != scratch(x)(y))
        world.getPatchAtWrap(x + minx, y + miny).setPatchVariable(vn, Double.box(scratch2(x)(y)))
    }
  }

  @throws(classOf[AgentException])
  @throws(classOf[PatchException])
  override def diffuse4(diffuseparam: Double, vn: Int): Unit = {
    val xx = world.worldWidth
    val yy = world.worldHeight
    val scratch: Array[Array[Double]] = world.getPatchScratch
    val scratch2: Array[Array[Double]] = Array.fill[Array[Double]](xx)(Array.fill(yy)(0.0d))
    val minx = world.minPxcor
    val miny = world.minPycor

    for {
      y <- 0 until yy
      x <- 0 until xx
    } {
      try {
        scratch(x)(y) =
          (world.fastGetPatchAt(x + minx, y + miny)
            .getPatchVariable(vn)
            .asInstanceOf[java.lang.Double])
            .doubleValue
            scratch2(x)(y) = 0
          } catch {
            case ex: ClassCastException =>
              throw new PatchException(world.fastGetPatchAt(wrapX(x).asInstanceOf[Int], wrapY(y).asInstanceOf[Int]))
          }
    }

    for {
      y <- yy until yy * 2
      x <- xx until xx * 2
    } {
      val diffuseVal = (scratch(x - xx)(y - yy) / 4) * diffuseparam

      if (x > 0 && x < xx - 1) {
        scratch2(x - xx)(y - yy) += scratch(x - xx)(y - yy) - (4 * diffuseVal)
        scratch2((x - 1) % xx)(y) += diffuseVal
        scratch2(x % xx)((y + 1) % yy) += diffuseVal
        scratch2(x % xx)((y - 1) % yy) += diffuseVal
        scratch2((x + 1) % xx)(y % yy) += diffuseVal
      } else if (x == xx) {
        scratch2(x - xx)(y - yy) += scratch(x - xx)(y - yy) - (3 * diffuseVal)
        scratch2(x % xx)((y + 1) % yy) += diffuseVal
        scratch2(x % xx)((y - 1) % yy) += diffuseVal
        scratch2((x + 1) % xx)(y % yy) += diffuseVal
      } else {
        scratch2(x - xx)(y - yy) += scratch(x - xx)(y - yy) - (3 * diffuseVal)
        scratch2(x % xx)((y + 1) % yy) += diffuseVal
        scratch2(x % xx)((y - 1) % yy) += diffuseVal
        scratch2((x - 1) % xx)(y % yy) += diffuseVal
      }
    }

    for {
      y <- 0 until yy
      x <- 0 until xx
    } {
      if (scratch2(x)(y) != scratch(x)(y))
        world.getPatchAtWrap(x + minx, y + miny).setPatchVariable(vn, Double.box(scratch2(x)(y)))
    }
  }

  override def observerX: Double = {
    0.0
  }

  override def followOffsetX: Double = {
    0.0
  }

  override def getNeighbors(source: Patch): AgentSet = {
    val neighbors =
      if (source.pxcor == world.maxPxcor) {
        if (source.pxcor == world.minPxcor) {
          if (source.pycor == world.maxPycor && source.pycor == world.minPycor)
            Array[Agent]()
          else
            Array[Agent](getPatchNorth(source), getPatchSouth(source))
        } else {
          if (source.pycor == world.maxPycor && source.pycor == world.minPycor)
            Array[Agent](getPatchWest(source))
          else
            Array[Agent](getPatchNorth(source), getPatchSouth(source),
              getPatchWest(source), getPatchSouthWest(source),
              getPatchNorthWest(source))
        }
    } else if (source.pxcor == world.minPxcor) {
      if (source.pycor == world.maxPycor && source.pycor == world.minPycor)
        Array[Agent](getPatchEast(source))
      else
        Array[Agent](getPatchNorth(source), getPatchEast(source),
          getPatchSouth(source), getPatchNorthEast(source),
          getPatchSouthEast(source))
    } else {
      if (source.pycor == world.maxPycor && source.pycor == world.minPycor)
        Array[Agent](getPatchEast(source), getPatchWest(source))
      else
        Array[Agent](getPatchNorth(source), getPatchEast(source),
          getPatchSouth(source), getPatchWest(source),
          getPatchNorthEast(source), getPatchSouthEast(source),
          getPatchSouthWest(source), getPatchNorthWest(source))
    }

    new ArrayAgentSet(AgentKind.Patch, neighbors.distinct)
  }

  override def getNeighbors4(source: Patch): AgentSet = {
    val neighbors =
      if (source.pxcor == world.maxPxcor) {
        if (source.pxcor == world.minPxcor) {
          if (source.pycor == world.maxPycor && source.pycor == world.minPycor)
            Array[Agent]()
          else
            Array[Agent](getPatchNorth(source), getPatchSouth(source))
        } else {
          if (source.pycor == world.maxPycor && source.pycor == world.minPycor)
            Array[Agent](getPatchWest(source))
          else
            Array[Agent](getPatchNorth(source), getPatchSouth(source), getPatchWest(source))
        }
      } else if (source.pxcor == world.minPxcor) {
        if (source.pycor == world.maxPycor && source.pycor == world.minPycor)
          Array[Agent](getPatchEast(source))
        else
          Array[Agent](getPatchNorth(source),
            getPatchEast(source),
            getPatchSouth(source))
      } else {
        if (source.pycor == world.maxPycor && source.pycor == world.minPycor)
          Array[Agent](getPatchEast(source), getPatchWest(source))
        else
          Array[Agent](getPatchNorth(source), getPatchEast(source),
            getPatchSouth(source), getPatchWest(source))
      }

    new ArrayAgentSet(AgentKind.Patch, neighbors.distinct)
  }

  //get patch

  override def getPN(source: Patch): Patch =
    getPatchNorth(source)

  override def getPE(source: Patch): Patch = {
    if (source.pxcor == world.maxPxcor)
      null
    else
      getPatchEast(source)
  }

  override def getPS(source: Patch): Patch =
    getPatchSouth(source)

  override def getPW(source: Patch): Patch = {
    if (source.pxcor == world.minPxcor)
      null
    else
      getPatchWest(source)
  }

  override def getPNE(source: Patch): Patch = {
    if (source.pxcor == world.maxPxcor)
      null
    else
      getPatchNorthEast(source)
  }

  override def getPSE(source: Patch): Patch = {
    if (source.pxcor == world.maxPxcor)
      null
    else
      getPatchSouthEast(source)
  }

  override def getPSW(source: Patch): Patch = {
    if (source.pxcor == world.minPxcor)
      null
    else
      getPatchSouthWest(source)
  }

  override def getPNW(source: Patch): Patch = {
    if (source.pxcor == world.minPxcor)
      null
    else
      getPatchNorthWest(source)
  }
}
