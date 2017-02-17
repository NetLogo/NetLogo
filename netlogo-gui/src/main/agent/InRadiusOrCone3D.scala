// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api.{ AgentException, Dump, Vect }
import org.nlogo.core.AgentKind

import java.lang.{ Long => JLong }
import java.util.{ ArrayList => JArrayList, HashSet => JHashSet, Iterator => JIterator, List => JList, RandomAccess }

import scala.collection.mutable.ArrayBuffer

object InRadiusOrCone3D {
  trait ThreeDRadialAgent[A <: Agent] extends InRadiusOrCone.RadialAgent[A] {
    def agentKind: AgentKind
    def x(a: A): Double
    def y(a: A): Double
    def z(a: A): Double
    def globalSet(w: World): AgentSet
    def foreachAgent(p: Patch, f: A => Unit): Unit
    def estimatedAgentCount(patchCount: Int, world: World): Long
  }

  implicit object ThreeDRadialPatch extends ThreeDRadialAgent[Patch3D] {
    def agentKind: AgentKind = AgentKind.Patch
    def x(a: Patch3D): Double = a.pxcor
    def y(a: Patch3D): Double = a.pycor
    def z(a: Patch3D): Double = a.pzcor
    def globalSet(w: World): AgentSet = w.patches
    def foreachAgent(p: Patch, f: Patch3D => Unit): Unit = f(p.asInstanceOf[Patch3D])
    def estimatedAgentCount(patchCount: Int, world: World): Long = patchCount
  }

  implicit object ThreeDRadialTurtle extends ThreeDRadialAgent[Turtle3D] {
    def agentKind: AgentKind = AgentKind.Turtle
    def x(a: Turtle3D): Double = a.xcor
    def y(a: Turtle3D): Double = a.ycor
    def z(a: Turtle3D): Double = a.zcor
    def globalSet(w: World): AgentSet = w.patches
    def foreachAgent(p: Patch, f: Turtle3D => Unit): Unit = {
      p.turtlesHere match {
        // this optimization may seem minor, but actually results in ~ 5-10% speedup
        // when finding any turtles in-radius
        case arrayList: JArrayList[_] =>
          var i = 0
          while (i < arrayList.size) {
            f(arrayList.get(i).asInstanceOf[Turtle3D])
            i += 1
          }
        case turtles =>
          val turtleIterator = turtles.iterator
          while (turtleIterator.hasNext) {
            f(turtleIterator.next().asInstanceOf[Turtle3D])
          }
      }
    }
    def estimatedAgentCount(patchCount: Int, world: World): Long =
      patchCount * (world.turtles.sizeBound / world.patches.sizeBound)
  }
}

import InRadiusOrCone._
import InRadiusOrCone3D._

class InRadiusOrCone3D(world3D: World3D) extends InRadiusOrCone(world3D) {

  override def generateRadiusStrategies(
    agent: Agent,
    sourceSet: AgentSet,
    radius: Double,
    wrap: Boolean): Seq[Strategy] = {
    val (startPatch, startX, startY, startZ) =
      agent match {
        case startTurtle: Turtle3D =>
          (startTurtle.getPatchHere.asInstanceOf[Patch3D], startTurtle.xcor(), startTurtle.ycor(), startTurtle.zcor)
        case patch: Patch3D =>
          (patch, patch.pxcor.toDouble, patch.pycor.toDouble, patch.pzcor.toDouble)
        case a =>
          throw new IllegalStateException("asked to find in-radius on unexpected agent: " + a)
      }

    val sourceKind = sourceSet.kind

    if (sourceKind == AgentKind.Patch) {
      val inRadius = new VerifyInRadius[Patch3D](startX, startY, startZ, radius, wrap)
      sharedStrategies[Patch3D](inRadius, sourceSet, world3D,
        new RadialPatchSetEnumeration(inRadius, _, radius, sourceSet, startPatch))
    } else if (sourceKind == AgentKind.Turtle) {
      val inRadius = new VerifyInRadius[Turtle3D](startX, startY, startZ, radius, wrap)
      val breededTurtles = new VerifyMembershipBreedset[Turtle3D](sourceSet, world3D)
      new RadialPatchSetEnumeration(inRadius, breededTurtles, radius, sourceSet, startPatch) +:
      sharedStrategies[Turtle3D](inRadius, sourceSet, world3D,
        new RadialPatchSetEnumeration(inRadius, _, radius, sourceSet, startPatch))
    } else
      Seq()
  }

  override def generateConeStrategies(
    callingTurtle: Turtle,
    sourceSet: AgentSet,
    radius: Double,
    angle: Double,
    wrap: Boolean): Seq[Strategy] = {
    val startTurtle = callingTurtle.asInstanceOf[Turtle3D]
    val startPatch = startTurtle.getPatchHere.asInstanceOf[Patch3D]

    val half = angle / 2.0

    if (sourceSet.kind == AgentKind.Patch) {
      val inCone = new VerifyInCone[Patch3D](startTurtle, radius, wrap, half)
      sharedStrategies[Patch3D](inCone, sourceSet, world3D,
        new ConicPatchSetEnumeration(inCone, _, radius, sourceSet, startPatch, world3D))
    } else {
      val inCone = new VerifyInCone[Turtle3D](startTurtle, radius, wrap, half)
      val breededTurtles = new VerifyMembershipBreedset[Turtle3D](sourceSet, world)
      new ConicPatchSetEnumeration(inCone, breededTurtles, radius, sourceSet, startPatch, world3D) +:
      sharedStrategies[Turtle3D](inCone, sourceSet, world3D,
        new ConicPatchSetEnumeration(inCone, _, radius, sourceSet, startPatch, world3D))
    }
  }

  private class VerifyInRadius[A <: Agent](startX: Double, startY: Double, startZ: Double, radius: Double, wrap: Boolean)(implicit pos: ThreeDRadialAgent[A]) extends VerifyGeometry[A] {
    val protractor = world.protractor.asInstanceOf[Protractor3D]

    def inGeometry(a: A) =
      protractor.distance(pos.x(a), pos.y(a), pos.z(a), startX, startY, startZ, wrap) <= radius
  }

  def sharedStrategies[A <: Agent](
    geometry: VerifyGeometry[A], sourceSet: AgentSet, world: World3D, f: VerifyMembership[A] => Strategy)(
      implicit ev: ThreeDRadialAgent[A]): Seq[Strategy] = {
    val allMembers       = new VerifyMembershipGlobal[A](world)
    val arbitraryMembers = new VerifyMembershipArbitrary[A](sourceSet)
    Seq(new ExhaustiveEnumeration[A](geometry, sourceSet), f(allMembers), f(arbitraryMembers))
  }

  private class VerifyInCone[A <: Agent](startTurtle: Turtle3D, radius: Double, wrap: Boolean, half: Double)(implicit pos: ThreeDRadialAgent[A]) extends VerifyGeometry[A] {
    val worldWidth = world.worldWidth
    val worldHeight = world.worldHeight
    val worldDepth = world3D.worldDepth
    val protractor = world3D.protractor.asInstanceOf[Protractor3D]
    val startX = startTurtle.xcor()
    val startY = startTurtle.ycor()
    val startZ = startTurtle.zcor()
    val turtleHeading = startTurtle.heading
    val turtlePitch = startTurtle.pitch

    // If wrap is true and the radius is large enough, the cone
    // wraps around the edges of the world.  We handle this by
    // re-attempting the in-cone search multiple times, offset by
    // the world width and height (as appropriate).
    // m and n are the maximum number of times the cone might wrap
    // around the edge of the world in the X and Y directions.
    // The searches are repeated for the values [-m, m] on the x axis,
    // [-n, n] on the y axis, [-k, k] on the z-axis.
    // So for each cone check we may perform (2n * 2m * 2k) separate
    // cone checks with the geometry offset.
    val (m, n, k) =
      if (wrap) {
        (if (world.wrappingAllowedInX) StrictMath.ceil(radius / worldWidth).toInt else 0,
         if (world.wrappingAllowedInY) StrictMath.ceil(radius / worldHeight).toInt else 0,
         if (world3D.wrappingAllowedInZ) StrictMath.ceil(radius / worldDepth).toInt else 0)
      } else {
        (0, 0, 0)
      }

    def inGeometry(a: A): Boolean = {
      val agentX = pos.x(a)
      val agentY = pos.y(a)
      val agentZ = pos.z(a)
      // we want to start with worldOffset 0 and work our way "outward"
      var worldOffsetX = 0
      while (worldOffsetX <= m) {
        var worldOffsetY = 0
        while (worldOffsetY <= n) {
          var worldOffsetZ = 0
          while (worldOffsetZ <= k) {
            if (isInCone(agentX + worldWidth * worldOffsetX, agentY + worldHeight * worldOffsetY, agentZ + worldDepth * worldOffsetZ))
              return true
            worldOffsetZ =
              if (worldOffsetZ < 0) (- worldOffsetZ) + 1
              else if (worldOffsetZ > 0) - worldOffsetZ
              else worldOffsetZ + 1
          }
          worldOffsetY =
            if (worldOffsetY < 0) (- worldOffsetY) + 1
            else if (worldOffsetY > 0) - worldOffsetY
            else worldOffsetY + 1
        }
        worldOffsetX =
          if (worldOffsetX < 0) (- worldOffsetX) + 1
          else if (worldOffsetX > 0) - worldOffsetX
          else worldOffsetX + 1
      }
      false
    }

    def isInCone(x: Double, y: Double, z: Double): Boolean = {
        if (x == startX && y == startY && z == startZ) {
          return true;
        }
        if (protractor.distance(startX, startY, startZ, x, y, z, false) > radius) // false = don't wrap, since inCone()
          // handles wrapping its own way
          {
            return false;
          }

          val unitVect = Vect.toVectors(turtleHeading, turtlePitch, 0)(0)
          val targetVect = new Vect(x - startX, y - startY, z - startZ)
          val angle = targetVect.angleTo(unitVect)
          val halfRadians = StrictMath.toRadians(half)

          return angle <= halfRadians || angle >= (2 * StrictMath.PI - halfRadians);
      }
  }

  private abstract class PatchSetEnumeration[A <: Agent](
    geometry: VerifyGeometry[A],
    membership: VerifyMembership[A],
    radius: Double,
    sourceSet: AgentSet,
    startPatch: Patch3D)(implicit ra: ThreeDRadialAgent[A]) extends Strategy {
    val name = s"Patch-based search on ${membership.name}"
    val rootsTable = world.rootsTable
    val ((dxmax, dxmin), (dymax, dymin), (dzmax, dzmin)) = computePatchRanges
    val numberOfPatches = (dxmax - dxmin) * (dymax - dymin) * (dzmax - dzmin)
    val estimatedAgentCount: Long = ra.estimatedAgentCount(numberOfPatches, world)
    def applies: Boolean = membership.appliesTo(sourceSet)
    def cost: Long = membership.cost + (numberOfPatches * CostToGetPatchAtOffset) +
      (estimatedAgentCount * (geometry.costPerAgent + membership.costPerAgent))
    def getPatch(dx: Int, dy: Int, dz: Int): Patch3D // this is different for radius / cone
    def findAgents: IndexedAgentSet = {
      membership.beginInclusionChecks()
      val result = new AgentSetBuilder(ra.agentKind, StrictMath.min(estimatedAgentCount, membership.estimatedSize).toInt)
      var dz = dzmin
      while (dz <= dzmax) {
        var dy = dymin
        while (dy <= dymax) {
          var dx = dxmin
          while (dx <= dxmax) {
            try {
              val gridRoot = StrictMath.sqrt(dx * dx + dy * dy + dz * dz)
              if (gridRoot <= radius + 1.415) {
                val patch = getPatch(dx, dy, dz)
                ra.foreachAgent(patch, { (a: A) =>
                  if (membership.includes(a) && geometry.inGeometry(a))
                    result.add(a)
                })
                // it's possible to optimize this further (at the cost of abstraction)
                // by skipping the Geometry check for in-radius if gridRoot < radius - 1.415
              }
            } catch {
              case e: AgentException => org.nlogo.api.Exceptions.ignore(e)
            }
            dx += 1
          }
          dy += 1
        }
        dz += 1
      }
      result.build()
    }

    private def computePatchRanges: ((Int, Int), (Int, Int), (Int, Int)) = {
      val r = StrictMath.ceil(radius).toInt

      (computePatchRange(world.wrappingAllowedInX,    world.worldWidth,   world.minPxcor,
          world.maxPxcor,   startPatch.pxcor, r),
        computePatchRange(world.wrappingAllowedInY,   world.worldHeight,  world.minPycor,
          world.maxPycor,   startPatch.pycor, r),
        computePatchRange(world3D.wrappingAllowedInZ, world3D.worldDepth, world3D.minPzcor,
          world3D.maxPzcor, startPatch.pzcor, r))
    }

    // make sure to use half the world dimensions rather than just max-p(x/y)cor
    // since when the origin is off-center that may actually be 0 and thus
    // nothing gets searched ev 9/12/07
    private def computePatchRange(
      wrappingAllowed: Boolean,
      worldDimension: Int,
      minDimension: Int,
      maxDimension: Int, start: Int, r: Int): (Int, Int) = {
        if (wrappingAllowed) {
          val dimension = worldDimension / 2.0
          if (r < dimension) (r, -r)
          else (StrictMath.floor(dimension).toInt, - StrictMath.ceil(dimension - 1).toInt)
        } else {
          val diff = minDimension - start
          (StrictMath.min(maxDimension - start, r), if (StrictMath.abs(diff) < r) diff else -r)
        }
      }
  }

  private class RadialPatchSetEnumeration[A <: Agent](geometry: VerifyGeometry[A], membership: VerifyMembership[A],
    radius: Double, sourceSet: AgentSet, startPatch: Patch3D)(implicit ra: ThreeDRadialAgent[A])
    extends PatchSetEnumeration(geometry, membership, radius, sourceSet, startPatch) {
    def getPatch(dx: Int, dy: Int, dz: Int): Patch3D = startPatch.getPatchAtOffsets(dx, dy, dz)
  }

  private class ConicPatchSetEnumeration[A <: Agent](geometry: VerifyGeometry[A], membership: VerifyMembership[A],
    radius: Double, sourceSet: AgentSet, startPatch: Patch3D, world: World3D)(implicit ra: ThreeDRadialAgent[A])
    extends PatchSetEnumeration(geometry, membership, radius, sourceSet, startPatch) {
    val startPxcor = startPatch.pxcor
    val startPycor = startPatch.pycor
    val startPzcor = startPatch.pzcor
    def getPatch(dx: Int, dy: Int, dz: Int): Patch3D =
      world.getPatchAtWrap(startPxcor + dx, startPycor + dy, startPzcor + dz).asInstanceOf[Patch3D]
  }
}
