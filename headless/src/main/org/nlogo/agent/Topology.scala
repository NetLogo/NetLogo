// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api.AgentException

@annotation.strictfp
object Topology {

  // factory method
  def get(world: World, xWrapping: Boolean, yWrapping: Boolean): Topology =
    (xWrapping, yWrapping) match {
      case (true , true ) => new Torus(world)
      case (true , false) => new VertCylinder(world)
      case (false, true ) => new HorizCylinder(world)
      case (false, false) => new Box(world)
    }

  // General wrapping function.
  def wrap(pos: Double, min: Double, max: Double): Double =
    if (pos >= max)
      min + ((pos - max) % (max - min))
    else if (pos < min) {
      val result = max - ((min - pos) % (max - min))
      // careful, if d is infinitesimal, then (max - d) might actually equal max!
      // but we must return an answer which is strictly less than max - ST 7/20/10
      if (result < max)
        result else min
    }
    else pos

}

abstract class Topology(val world: World) {

  @throws(classOf[AgentException])
  def wrapX(x: Double): Double
  @throws(classOf[AgentException])
  def wrapY(y: Double): Double
  @throws(classOf[AgentException])
  def getPatchAt(xc: Double, yc: Double): Patch

  def distanceWrap(dx: Double, dy: Double, x1: Double, y1: Double, x2: Double, y2: Double): Double
  def towardsWrap(headingX: Double, headingY: Double): Double

  def getNeighbors(source: Patch): AgentSet
  def getNeighbors4(source: Patch): AgentSet
  def getPN(source: Patch): Patch
  def getPE(source: Patch): Patch
  def getPS(source: Patch): Patch
  def getPW(source: Patch): Patch
  def getPNE(source: Patch): Patch
  def getPSE(source: Patch): Patch
  def getPSW(source: Patch): Patch
  def getPNW(source: Patch): Patch
  def shortestPathX(x1: Double, x2: Double): Double
  def shortestPathY(y1: Double, y2: Double): Double

  ///

  def observerX: Double = world.observer.oxcor
  def observerY: Double = world.observer.oycor
  def followOffsetX: Double = world.observer.followOffsetX
  def followOffsetY: Double = world.observer.followOffsetY

  @throws(classOf[AgentException])
  @throws(classOf[PatchException])
  def diffuse(diffuseparam: Double, vn: Int)

  @throws(classOf[AgentException])
  @throws(classOf[PatchException])
  def diffuse4(diffuseparam: Double, vn: Int)

  /*
  // generic diffusers, they should work for any topology
  // but you may also override to optimize diffusing
  // all the current topologies have custom diffusers
  // but keep this around in case we add more ev 9/28/05, 10/20/05
  void diffuse(Double diffuseparam, int vn)
      throws AgentException, PatchException {
    int minx = world.minPxcor();
    int maxx = world.maxPxcor();
    int miny = world.minPycor();
    int maxy = world.maxPycor();

    Double sum, newval, oldval;
    Double[][] scratchOld = world.getPatchScratch();
    Double[][] scratchNew = new Double[maxx - minx + 1][maxy - miny + 1];
    int x = minx;
    int y = miny;

    try {
      for (x = minx; x <= maxx; x++) {
        for (y = miny; y <= maxy; y++) {
          Patch patch = world.fastGetPatchAt(x, y);

          oldval = ((Double) patch.getPatchVariable(vn)).DoubleValue();

          AgentSet neighbors = patch.getNeighbors();
          int neighborCount = neighbors.count();
          sum = 0;
          for (int a = 0; a < neighborCount; a++) {
            Patch p = (Patch) neighbors.agent(a);
            sum += (((Double) p.getPatchVariable(vn)).DoubleValue() / p.getNeighbors().count());
          }

          scratchOld[x - minx][y - miny] = oldval;
          scratchNew[x - minx][y - miny] = oldval * (1 - diffuseparam)
              + sum * diffuseparam;
        }
      }
      for (x = minx; x <= maxx; x++) {
        for (y = miny; y <= maxy; y++) {
          newval = scratchNew[x - minx][y - miny];
          if (newval != scratchOld[x - minx][y - miny]) {
            world.fastGetPatchAt(x, y).setPatchVariable(vn, Double.valueOf(newval));
          }
        }
      }

    } catch (ClassCastException ex) {
      throw new PatchException(world.fastGetPatchAt(x, y));
    }
  }

  void diffuse4(Double diffuseparam, int vn)
      throws AgentException, PatchException {
    int minx = world.minPxcor();
    int maxx = world.maxPxcor();
    int miny = world.minPycor();
    int maxy = world.maxPycor();

    Double sum, newval, oldval;
    Double[][] scratchOld = world.getPatchScratch();
    Double[][] scratchNew = new Double[maxx - minx + 1][maxy - miny + 1];
    int x = minx;
    int y = miny;
    try {
      for (x = minx; x <= maxx; x++) {
        for (y = miny; y <= maxy; y++) {
          Patch patch = world.fastGetPatchAt(x, y);

          oldval = ((Double) patch.getPatchVariable(vn)).DoubleValue();

          AgentSet neighbors = patch.getNeighbors4();
          int neighborCount = neighbors.count();
          sum = 0;
          for (int a = 0; a < neighborCount; a++) {
            sum += ((Double) ((Patch) neighbors.agent(a))
                .getPatchVariable(vn))
                .DoubleValue();
          }

          scratchOld[x - minx][y - miny] = oldval;
          scratchNew[x - minx][y - miny] = oldval * (1 - diffuseparam)
              + sum * diffuseparam / neighborCount;
        }
      }
      for (x = minx; x <= maxx; x++) {
        for (y = miny; y <= maxy; y++) {
          newval = scratchNew[x - minx][y - miny];
          if (newval != scratchOld[x - minx][y - miny]) {
            world.fastGetPatchAt(x, y).setPatchVariable(vn, Double.valueOf(newval));
          }
        }
      }
    } catch (ClassCastException ex) {
      throw new PatchException(world.fastGetPatchAt(x, y));
    }
  }
  */

  // getPatch methods.  These are here so they can be called by subclasses in their implementations
  // of getPN, getPS, etc.  They provide the usual torus-style behavior.  It's a little odd that
  // they're here rather than in Torus, but doing it that way would have involved other
  // awkwardnesses -- not clear to me right now (ST) what the best way to setup this up would be.
  // One suboptimal thing about how it's set up right now is that e.g. in subclass methods like
  // Box.getPN, the source.pycor gets tested once, and then if Box.getPN calls
  // Topology.getPatchNorth, then source.pycor gets redundantly tested again.
  // - JD, ST 6/3/04

  def getPatchNorth(source: Patch): Patch =
    source.fastGetPatchAt(
      source.pxcor,
      if (source.pycor == world.maxPycor)
        world.minPycor
      else
        source.pycor + 1)

  def getPatchSouth(source: Patch): Patch =
    source.fastGetPatchAt(
      source.pxcor,
      if (source.pycor == world.minPycor)
        world.maxPycor
      else
        source.pycor - 1)

  def getPatchEast(source: Patch): Patch =
    source.fastGetPatchAt(
      if (source.pxcor == world.maxPxcor)
        world.minPxcor
      else
        source.pxcor + 1,
      source.pycor)

  def getPatchWest(source: Patch): Patch =
    source.fastGetPatchAt(
      if (source.pxcor == world.minPxcor)
        world.maxPxcor
      else
        source.pxcor - 1,
      source.pycor)

  def getPatchNorthWest(source: Patch): Patch =
    source.fastGetPatchAt(
      if (source.pxcor == world.minPxcor)
        world.maxPxcor
      else
        source.pxcor - 1,
      if (source.pycor == world.maxPycor)
        world.minPycor
      else
        source.pycor + 1)

  def getPatchSouthWest(source: Patch): Patch =
    source.fastGetPatchAt(
      if (source.pxcor == world.minPxcor)
        world.maxPxcor
      else
        source.pxcor - 1,
      if (source.pycor == world.minPycor)
        world.maxPycor
      else
        source.pycor - 1)

  def getPatchSouthEast(source: Patch): Patch =
    source.fastGetPatchAt(
      if (source.pxcor == world.maxPxcor)
        world.minPxcor
      else
        source.pxcor + 1,
      if (source.pycor == world.minPycor)
        world.maxPycor
      else
        source.pycor - 1)

  def getPatchNorthEast(source: Patch): Patch =
    source.fastGetPatchAt(
      if (source.pxcor == world.maxPxcor)
        world.minPxcor
      else
        source.pxcor + 1,
      if (source.pycor == world.maxPycor)
        world.minPycor
      else
        source.pycor + 1)

}
