// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api.{ AgentException, Dump }
import org.nlogo.core.AgentKind

import java.lang.{ Long => JLong }
import java.util.{ ArrayList => JArrayList, HashSet => JHashSet, Iterator => JIterator, List => JList, RandomAccess }

import scala.collection.mutable.ArrayBuffer

object InRadiusOrCone {
  /*
   * A note on optimizations in this file.
   *
   * The optimizations here are performed based on relative benchmarked times for the following
   * operations. These benchmarks were performed on Java 8u121. If `in-radius` performance suffers,
   * rebenchmark these and adjust relative times.
   */
  /** Relative Amount of time to run `getPatchAtOffset` */
  val CostToGetPatchAtOffset = 6
  /** Relative Amount of time to run compute distance of turtle via world.protractor.distance */
  val CostToComputeDistance = 4
  /** Relative Amount of time to insert a Long (agent id) into a HashSet */
  val CostToInsertID = 2
  /** Relative Amount of time to check whether a HashSet contains a Long (agent id) */
  val CostOfContainsID = 1

  trait Strategy {
    def name: String
    def applies: Boolean
    def cost: Long
    def findAgents: IndexedAgentSet
  }

  trait RadialAgent[A <: Agent] {
    def agentKind: AgentKind
    def x(a: A): Double
    def y(a: A): Double
    def globalSet(w: World): AgentSet
    def foreachAgent(p: Patch, f: A => Unit): Unit
    def estimatedAgentCount(patchCount: Int, world: World): Long
  }

  implicit object RadialAgentPatch extends RadialAgent[Patch] {
    val agentKind = AgentKind.Patch
    def x(patch: Patch): Double = patch.pxcor
    def y(patch: Patch): Double = patch.pycor
    def globalSet(world: World): AgentSet = world.patches
    def foreachAgent(p: Patch, f: Patch => Unit): Unit = f(p)
    def estimatedAgentCount(patchCount: Int, world: World): Long = patchCount
  }

  implicit object RadialAgentTurtle extends RadialAgent[Turtle] {
    val agentKind = AgentKind.Turtle
    def x(turtle: Turtle): Double = turtle.xcor
    def y(turtle: Turtle): Double = turtle.ycor
    def globalSet(world: World): AgentSet = world.turtles
    def foreachAgent(p: Patch, f: Turtle => Unit): Unit = {
      p.turtlesHere match {
        // this optimization may seem minor, but actually results in ~ 5-10% speedup
        // when finding any turtles in-radius
        case arrayList: JArrayList[_] =>
          var i = 0
          while (i < arrayList.size) {
            f(arrayList.get(i).asInstanceOf[Turtle])
            i += 1
          }
        case turtles =>
          val turtleIterator = turtles.iterator
          while (turtleIterator.hasNext) {
            f(turtleIterator.next().asInstanceOf[Turtle])
          }
      }
    }
    def estimatedAgentCount(patchCount: Int, world: World): Long =
      patchCount * (world.turtles.sizeBound / world.patches.sizeBound)
  }

  trait VerifyMembership[A <: Agent] {
    val costPerAgent: Long
    val estimatedSize: Int
    def cost: Long
    def appliesTo(agentSet: AgentSet): Boolean
    def beginInclusionChecks(): Unit = {}
    def includes(a: A): Boolean
    def name: String
  }

  class VerifyMembershipGlobal[A <: Agent](world: World)(implicit ra: RadialAgent[A])
    extends VerifyMembership[A] {
    val name = "all agents"
    val globalSet = ra.globalSet(world)
    val costPerAgent = 0
    def cost = 0
    val estimatedSize = globalSet.sizeBound
    def appliesTo(agentSet: AgentSet): Boolean = agentSet eq globalSet
    def includes(a: A): Boolean = true
  }

  class VerifyMembershipBreedset[T <: Turtle](breedSet: AgentSet, world: World)(implicit ra: RadialAgent[Turtle])
    extends VerifyMembership[T] {
    val name = "turtle breedset"
    val globalSet = ra.globalSet(world)
    val costPerAgent = 0
    def cost = 0
    val estimatedSize = breedSet.sizeBound
    def appliesTo(agentSet: AgentSet): Boolean = (agentSet ne globalSet) && agentSet.isBreedSet
    def includes(a: T): Boolean = a.getBreed == breedSet
  }

  class VerifyMembershipArbitrary[A <: Agent](sourceSet: AgentSet)(implicit ra: RadialAgent[A])
    extends VerifyMembership[A] {
    val name = "arbitrary agentset"
    var idCache: JHashSet[JLong] = _ // avoid allocating until we know we'll use this filter
    val costPerAgent = CostOfContainsID
    val cost = sourceSet.sizeBound * CostToInsertID
    val estimatedSize = sourceSet.sizeBound
    def appliesTo(agentSet: AgentSet): Boolean = ! agentSet.isBreedSet
    override def beginInclusionChecks(): Unit = {
      idCache = new JHashSet[JLong](sourceSet.sizeBound)
      val agentIterator = sourceSet.iterator
      while (agentIterator.hasNext) {
        idCache.add(new JLong(agentIterator.next().id))
      }
    }
    def includes(a: A): Boolean = idCache.contains(new JLong(a.id()))
  }

  trait VerifyGeometry[A <: Agent] {
    val costPerAgent = CostToComputeDistance
    def inGeometry(a: A): Boolean
  }

  class ExhaustiveEnumeration[A <: Agent](geometry: VerifyGeometry[A], sourceSet: AgentSet)(implicit ra: RadialAgent[A]) extends Strategy {
    val name = s"Check each agent in ${Dump.logoObject(sourceSet, false, false)}"
    def applies: Boolean = true
    def cost = sourceSet.sizeBound * geometry.costPerAgent
    def findAgents: IndexedAgentSet = {
      val result = new AgentSetBuilder(ra.agentKind, sourceSet.sizeBound)
      val agentIterator = sourceSet.iterator
      while (agentIterator.hasNext) {
        try {
          val a = agentIterator.next().asInstanceOf[A]
          if (geometry.inGeometry(a))
            result.add(a)
        } catch {
          case e: AgentException => org.nlogo.api.Exceptions.ignore(e)
        }
      }
      result.build()
    }
  }
}

import InRadiusOrCone._

class InRadiusOrCone(val world: World) {

    /* A note on how we optimize this file:
     *
     * There are two general approaches we can use to find all the turtles:
     * 1. Exhaustively examine each agent in the source set and check whether the agent is within
     *    `radius` distance (or within the cone) of `startX` and `startY`.
     * 2. Pull all agents out of the relevant patches at nearby offsets and check whether
     *    the agent is within `radius` distance (or within the cone) of `startX` and `startY`
     *    *and* whether the agent belongs to the sourceSet.
     *
     * Cost of approach 1 (turtles) = `CostToComputeDistance` * `sourceSet.size`
     * Cost of approach 2 (turtles, non-breed sourceSet) =
     *  `CostToGetPatchAtOffset` * <number of patches> +
     *   (`CostToComputeDistance` + `CostOfContainsID`) * <agents on patches> +
     *   `CostToInsertID` * `sourceSet.size`
     * Cost of approach 2 (turtles, breeded sourceSet) =
     *  `CostToGetPatchAtOffset` * <number of patches> +
     *   `CostToComputeDistance` * <agents on patches>
     *
     *  There isn't a fast way to compute agents on patches, so we use the following number
     *  as a heuristic:
     *  (<total number of turtles in world> / <total number of patches in the world>) * <number of patches>
     *
     * Similarly, there are two techniques for patches:
     * 1. Exhaustively examine each agent in the source patch set and check that the agent is within
     *    `radius` distance (or within the cone) of `startX` and `startY`.
     * 2. Pull all relevant patches at nearby offsets and check whether the agent is within
     *    `radius` distance (or within the cone) of `startX` and `startY` *and*
     *    whether the agent belongs to the sourceSet.
     *
     * Cost of approach 1 = `CostToComputeDistance` * `sourceSet.size`
     * Cost of approach 2 if (sourceSet != world.patches) =
     *  (`CostToGetPatchAtOffset` + `CostOfContainsID` + `CostToComputeDistance`) * <number of patches> +
     *   `CostToInsertID` * `sourceSet.size`
     * Cost of approach 2 if (sourceSet == world.patches) =
     *  (`CostToGetPatchAtOffset` + `CostToComputeDistance`) * <number of patches>
     */

  def inRadius(
     agent:     Agent,
     sourceSet: AgentSet,
     radius:    Double,
     wrap:      Boolean): IndexedAgentSet = {
    val strategies = generateRadiusStrategies(agent, sourceSet, radius, wrap)
    runBestStrategy(strategies, sourceSet.kind)
  }

  def inCone(
    startTurtle: Turtle,
    sourceSet:   AgentSet,
    radius:      Double,
    angle:       Double,
    wrap:        Boolean): IndexedAgentSet = {
    val strategies = generateConeStrategies(startTurtle, sourceSet, radius, angle, wrap)
    runBestStrategy(strategies, sourceSet.kind)
  }

  private[agent] def radiusStrategiesSummary(
    agent: Agent,
    sourceSet: AgentSet,
    radius: Double,
    wrap: Boolean): Seq[(Boolean, String, Long)] = {
      val strategies = generateRadiusStrategies(agent, sourceSet, radius, wrap)
      strategies.sortBy(_.cost).map(s => (s.applies, s.name, s.cost))
  }

  private[agent] def coneStrategiesSummary(
    startTurtle: Turtle,
    sourceSet:   AgentSet,
    radius:      Double,
    angle:       Double,
    wrap:        Boolean): Seq[(Boolean, String, Long)] = {
      val strategies = generateConeStrategies(startTurtle, sourceSet, radius, angle, wrap)
      strategies.sortBy(_.cost).map(s => (s.applies, s.name, s.cost))
  }

  protected def generateRadiusStrategies(
    agent: Agent,
    sourceSet: AgentSet,
    radius: Double,
    wrap: Boolean): Seq[Strategy] = {
    val (startPatch, startX, startY) =
      agent match {
        case startTurtle: Turtle =>
          (startTurtle.getPatchHere, startTurtle.xcor, startTurtle.ycor)
        case patch: Patch =>
          (patch, patch.pxcor.toDouble, patch.pycor.toDouble)
        case a =>
          throw new IllegalStateException("asked to find in-radius on unexpected agent: " + a)
      }

    val sourceKind = sourceSet.kind

    if (sourceKind == AgentKind.Patch) {
      val inRadius = new VerifyInRadius[Patch](startX, startY, radius, wrap)
      sharedStrategies[Patch](inRadius, sourceSet, world,
        new RadialPatchSetEnumeration(inRadius, _, radius, sourceSet, startPatch))
    } else if (sourceKind == AgentKind.Turtle) {
      val inRadius = new VerifyInRadius[Turtle](startX, startY, radius, wrap)
      val breededTurtles = new VerifyMembershipBreedset[Turtle](sourceSet, world)
      new RadialPatchSetEnumeration(inRadius, breededTurtles, radius, sourceSet, startPatch) +:
      sharedStrategies[Turtle](inRadius, sourceSet, world,
        new RadialPatchSetEnumeration(inRadius, _, radius, sourceSet, startPatch))
    } else
      Seq()
  }

  protected def generateConeStrategies(
    startTurtle: Turtle,
    sourceSet: AgentSet,
    radius: Double,
    angle: Double,
    wrap: Boolean): Seq[Strategy] = {
    val startPatch = startTurtle.getPatchHere

    val half = angle / 2.0

    if (sourceSet.kind == AgentKind.Patch) {
      val inCone = new VerifyInCone[Patch](startTurtle, radius, wrap, half)
      sharedStrategies[Patch](inCone, sourceSet, world,
        new ConicPatchSetEnumeration(inCone, _, radius, sourceSet, startPatch, world))
    } else {
      val inCone = new VerifyInCone[Turtle](startTurtle, radius, wrap, half)
      val breededTurtles = new VerifyMembershipBreedset[Turtle](sourceSet, world)
      new ConicPatchSetEnumeration(inCone, breededTurtles, radius, sourceSet, startPatch, world) +:
      sharedStrategies[Turtle](inCone, sourceSet, world,
        new ConicPatchSetEnumeration(inCone, _, radius, sourceSet, startPatch, world))
    }
  }

  private def sharedStrategies[A <: Agent](
    geometry: VerifyGeometry[A], sourceSet: AgentSet, world: World, f: VerifyMembership[A] => Strategy)(
      implicit ev: RadialAgent[A]): Seq[Strategy] = {
    val allMembers       = new VerifyMembershipGlobal[A](world)
    val arbitraryMembers = new VerifyMembershipArbitrary[A](sourceSet)
    Seq(new ExhaustiveEnumeration[A](geometry, sourceSet), f(allMembers), f(arbitraryMembers))
  }

  private def runBestStrategy(strategies: Seq[Strategy], kind: AgentKind): IndexedAgentSet = {
    strategies
      .filter(_.applies)
      .sortBy(_.cost)
      .headOption
      .map(_.findAgents)
      .getOrElse(new AgentSetBuilder(kind, 0).build())
  }

  private class VerifyInRadius[A <: Agent](startX: Double, startY: Double, radius: Double, wrap: Boolean)(implicit pos: RadialAgent[A]) extends VerifyGeometry[A] {
    val protractor = world.protractor

    def inGeometry(a: A) =
      protractor.distance(pos.x(a), pos.y(a), startX, startY, wrap) <= radius
  }

  private class VerifyInCone[A <: Agent](startTurtle: Turtle, radius: Double, wrap: Boolean, half: Double)(implicit pos: RadialAgent[A]) extends VerifyGeometry[A] {
    val worldWidth = world.worldWidth
    val worldHeight = world.worldHeight
    val protractor = world.protractor
    val startX = startTurtle.xcor
    val startY = startTurtle.ycor
    val turtleHeading = startTurtle.heading

    // If wrap is true and the radius is large enough, the cone
    // wraps around the edges of the world.  We handle this by
    // re-attempting the in-cone search multiple times, offset by
    // the world width and height (as appropriate).
    // m and n are the maximum number of times the cone might wrap
    // around the edge of the world in the X and Y directions.
    // The searches are repeated for the values [-m, m] on the x axis
    // and [-n, n] on the y axis. So for each cone check we may perform
    // (2n * 2m) separate cone checks with the geometry offset.
    val (m, n) =
      if (wrap) {
        (if (world.wrappingAllowedInX) StrictMath.ceil(radius / worldWidth).toInt else 0,
         if (world.wrappingAllowedInY) StrictMath.ceil(radius / worldHeight).toInt else 0)
      } else {
        (0, 0)
      }

    def inGeometry(a: A): Boolean = {
      val agentX = pos.x(a)
      val agentY = pos.y(a)
      // we want to start with worldOffset 0 and work our way "outward"
      var worldOffsetX = 0
      while (worldOffsetX <= m) {
        var worldOffsetY = 0
        while (worldOffsetY <= n) {
          if (isInCone(agentX + worldWidth * worldOffsetX, agentY + worldHeight * worldOffsetY))
            return true
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

    // helper method for inCone().
    // check if (x, y) is in the cone with center (startX, startY) , radius radius,
    // half-angle half, and central line of the cone having heading turtleHeading.
    private def isInCone(x: Double, y: Double): Boolean = {
      if (x == startX && y == startY) {
        true
      } else if (protractor.distance(startX, startY, x, y, false) > radius) {
        // handles wrapping its own way
        false
      } else {
        try {
          val theta = protractor.towards(startX, startY, x, y, false)
          val diff = StrictMath.abs(theta - turtleHeading)
          // we have to be careful here because e.g. the difference between 5 and 355
          // is 10 not 350... hence the 360 thing
          (diff <= half) || ((360 - diff) <= half)
        } catch {
          // this should never happen because towards() only throws an AgentException
          // when the distance is 0, but we already ruled out that case above
          case e: AgentException =>
            throw new IllegalStateException(e.toString)
        }
      }
    }
  }

  private abstract class PatchSetEnumeration[A <: Agent](geometry: VerifyGeometry[A], membership: VerifyMembership[A], radius: Double, sourceSet: AgentSet, startPatch: Patch)(implicit ra: RadialAgent[A]) extends Strategy {
    val name = s"Patch-based search on ${membership.name}"
    val rootsTable = world.rootsTable
    val ((dxmax, dxmin), (dymax, dymin)) = computePatchRanges
    val numberOfPatches = (dxmax - dxmin) * (dymax - dymin)
    val estimatedAgentCount: Long = ra.estimatedAgentCount(numberOfPatches, world)
    def applies: Boolean = membership.appliesTo(sourceSet)
    def cost: Long = membership.cost + (numberOfPatches * CostToGetPatchAtOffset) +
      (estimatedAgentCount * (geometry.costPerAgent + membership.costPerAgent))
    def getPatch(dx: Int, dy: Int): Patch // this is different for radius / cone
    def findAgents: IndexedAgentSet = {
      membership.beginInclusionChecks()
      val result = new AgentSetBuilder(ra.agentKind, StrictMath.min(estimatedAgentCount, membership.estimatedSize).toInt)
      var dy = dymin
      while (dy <= dymax) {
        var dx = dxmin
        while (dx <= dxmax) {
          try {
            val gridRoot = rootsTable.gridRoot(dx * dx + dy * dy)
            if (gridRoot <= radius + 1.415) {
              val patch = getPatch(dx, dy)
              ra.foreachAgent(patch, { a =>
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
      result.build()
    }

    private def computePatchRanges: ((Int, Int), (Int, Int)) = {
      val r = StrictMath.ceil(radius).toInt

      (computePatchRange(world.wrappingAllowedInX, world.worldWidth, world.minPxcor, world.maxPxcor, startPatch.pxcor, r),
        computePatchRange(world.wrappingAllowedInY, world.worldHeight, world.minPycor, world.maxPycor, startPatch.pycor, r))
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
    radius: Double, sourceSet: AgentSet, startPatch: Patch)(implicit ra: RadialAgent[A])
    extends PatchSetEnumeration(geometry, membership, radius, sourceSet, startPatch) {
    def getPatch(dx: Int, dy: Int): Patch = startPatch.getPatchAtOffsets(dx, dy)
  }

  private class ConicPatchSetEnumeration[A <: Agent](geometry: VerifyGeometry[A], membership: VerifyMembership[A],
    radius: Double, sourceSet: AgentSet, startPatch: Patch, world: World)(implicit ra: RadialAgent[A])
    extends PatchSetEnumeration(geometry, membership, radius, sourceSet, startPatch) {
    val startPxcor = startPatch.pxcor
    val startPycor = startPatch.pycor
    def getPatch(dx: Int, dy: Int): Patch =
      world.getPatchAtWrap(startPxcor + dx, startPycor + dy)
  }
}
