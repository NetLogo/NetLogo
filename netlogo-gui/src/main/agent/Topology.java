// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

import org.nlogo.api.AgentException;

abstract strictfp class Topology {

  final World world;

  Topology(World world) {
    this.world = world;
  }

  abstract double wrapX(double x) throws AgentException;

  abstract double wrapY(double y) throws AgentException;

  abstract double distanceWrap(double dx, double dy, double x1, double y1, double x2, double y2);

  abstract double towardsWrap(double headingX, double headingY);

  //getPatch methods
  abstract Patch getPatchAt(double xc, double yc) throws AgentException;

  abstract IndexedAgentSet getNeighbors(Patch source);

  abstract IndexedAgentSet getNeighbors4(Patch source);

  abstract Patch getPN(Patch source);

  abstract Patch getPE(Patch source);

  abstract Patch getPS(Patch source);

  abstract Patch getPW(Patch source);

  abstract Patch getPNE(Patch source);

  abstract Patch getPSE(Patch source);

  abstract Patch getPSW(Patch source);

  abstract Patch getPNW(Patch source);

  abstract double shortestPathX(double x1, double x2);

  abstract double shortestPathY(double y1, double y2);

  /// factory method

  public static Topology getTopology(World world, boolean xWrapping, boolean yWrapping) {
    if (xWrapping) {
      if (yWrapping) {
        return new Torus(world);
      } else {
        return new VertCylinder(world);
      }
    } else {
      if (yWrapping) {
        return new HorizCylinder(world);
      } else {
        return new Box(world);
      }
    }
  }

  ///

  // General wrapping function.
  static double wrap(double pos, double min, double max) {
    if (pos >= max) {
      return (min + ((pos - max) % (max - min)));
    } else if (pos < min) {
      double result = max - ((min - pos) % (max - min));
      // careful, if d is infinitesimal, then (max - d) might actually equal max!
      // but we must return an answer which is strictly less than max - ST 7/20/10
      return (result < max)
          ? result
          : min;
    } else {
      return pos;
    }
  }

  double observerX() {
    return world.observer().oxcor();
  }

  double observerY() {
    return world.observer().oycor();
  }

  double followOffsetX() {
    return world.observer().followOffsetX();
  }

  double followOffsetY() {
    return world.observer().followOffsetY();
  }

  // generic diffusers, they should work for any topology
  // but you may also override to optimize diffusing
  // all the current topologies have custom diffusers
  // but keep this around in case we add more ev 9/28/05, 10/20/05
  void diffuse(double diffuseparam, int vn)
      throws AgentException, PatchException {
    int minx = world.minPxcor();
    int maxx = world.maxPxcor();
    int miny = world.minPycor();
    int maxy = world.maxPycor();

    double sum, newval, oldval;
    double[][] scratchOld = world.getPatchScratch();
    double[][] scratchNew = new double[maxx - minx + 1][maxy - miny + 1];
    int x = minx;
    int y = miny;

    try {
      for (x = minx; x <= maxx; x++) {
        for (y = miny; y <= maxy; y++) {
          Patch patch = world.fastGetPatchAt(x, y);

          oldval = ((Double) patch.getPatchVariable(vn)).doubleValue();

          IndexedAgentSet neighbors = patch.getNeighbors();
          int neighborCount = neighbors.count();
          sum = 0;
          for (int i = 0; i < neighborCount; i++) {
            Patch p = (Patch) neighbors.getByIndex(i);
            sum += (((Double) p.getPatchVariable(vn)).doubleValue() / p.getNeighbors().count());
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

  void diffuse4(double diffuseparam, int vn)
      throws AgentException, PatchException {
    int minx = world.minPxcor();
    int maxx = world.maxPxcor();
    int miny = world.minPycor();
    int maxy = world.maxPycor();

    double sum, newval, oldval;
    double[][] scratchOld = world.getPatchScratch();
    double[][] scratchNew = new double[maxx - minx + 1][maxy - miny + 1];
    int x = minx;
    int y = miny;
    try {
      for (x = minx; x <= maxx; x++) {
        for (y = miny; y <= maxy; y++) {
          Patch patch = world.fastGetPatchAt(x, y);

          oldval = ((Double) patch.getPatchVariable(vn)).doubleValue();

          IndexedAgentSet neighbors = patch.getNeighbors4();
          int neighborCount = neighbors.count();
          sum = 0;
          for (int i = 0; i < neighborCount; i++) {
            sum += ((Double) ((Patch) neighbors.getByIndex(i))
                .getPatchVariable(vn))
                .doubleValue();
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

  // getPatch methods.  These are here so they can be called by
  // subclasses in their implementations of getPN, getPS, etc.
  // They provide the usual torus-style behavior.
  // It's a little odd that they're here rather than in Torus,
  // but doing it that way would have involved other awkwardnesses
  // -- not clear to me right now (ST) what the best way to setup
  // this up would be.  One suboptimal thing about how it's set up
  // right now is that e.g. in subclass methods like Box.getPN,
  // the source.pycor gets tested once, and then if Box.getPN
  // calls Topology.getPatchNorth, then source.pycor gets redundantly
  // tested again.
  // - JD, ST 6/3/04

  Patch getPatchNorth(Patch source) {
    if (source.pycor == world.maxPycor()) {
      return source.fastGetPatchAt(source.pxcor, world.minPycor());
    } else {
      return source.fastGetPatchAt(source.pxcor, source.pycor + 1);
    }
  }

  Patch getPatchSouth(Patch source) {
    if (source.pycor == world.minPycor()) {
      return source.fastGetPatchAt(source.pxcor, world.maxPycor());
    } else {
      return source.fastGetPatchAt(source.pxcor, source.pycor - 1);
    }
  }

  Patch getPatchEast(Patch source) {
    if (source.pxcor == world.maxPxcor()) {
      return source.fastGetPatchAt(world.minPxcor(), source.pycor);
    } else {
      return source.fastGetPatchAt(source.pxcor + 1, source.pycor);
    }
  }

  Patch getPatchWest(Patch source) {
    if (source.pxcor == world.minPxcor()) {
      return source.fastGetPatchAt(world.maxPxcor(), source.pycor);
    } else {
      return source.fastGetPatchAt(source.pxcor - 1, source.pycor);
    }
  }

  Patch getPatchNorthWest(Patch source) {
    if (source.pycor == world.maxPycor()) {
      if (source.pxcor == world.minPxcor()) {
        return source.fastGetPatchAt(world.maxPxcor(), world.minPycor());
      } else {
        return source.fastGetPatchAt(source.pxcor - 1, world.minPycor());
      }
    } else if (source.pxcor == world.minPxcor()) {
      return source.fastGetPatchAt(world.maxPxcor(), source.pycor + 1);
    } else {
      return source.fastGetPatchAt(source.pxcor - 1, source.pycor + 1);
    }
  }

  Patch getPatchSouthWest(Patch source) {
    if (source.pycor == world.minPycor()) {
      if (source.pxcor == world.minPxcor()) {
        return source.fastGetPatchAt(world.maxPxcor(), world.maxPycor());
      } else {
        return source.fastGetPatchAt(source.pxcor - 1, world.maxPycor());
      }
    } else if (source.pxcor == world.minPxcor()) {
      return source.fastGetPatchAt(world.maxPxcor(), source.pycor - 1);
    } else {
      return source.fastGetPatchAt(source.pxcor - 1, source.pycor - 1);
    }
  }

  Patch getPatchSouthEast(Patch source) {
    if (source.pycor == world.minPycor()) {
      if (source.pxcor == world.maxPxcor()) {
        return source.fastGetPatchAt(world.minPxcor(), world.maxPycor());
      } else {
        return source.fastGetPatchAt(source.pxcor + 1, world.maxPycor());
      }
    } else if (source.pxcor == world.maxPxcor()) {
      return source.fastGetPatchAt(world.minPxcor(), source.pycor - 1);
    } else {
      return source.fastGetPatchAt(source.pxcor + 1, source.pycor - 1);
    }
  }

  Patch getPatchNorthEast(Patch source) {
    if (source.pycor == world.maxPycor()) {
      if (source.pxcor == world.maxPxcor()) {
        return source.fastGetPatchAt(world.minPxcor(), world.minPycor());
      } else {
        return source.fastGetPatchAt(source.pxcor + 1, world.minPycor());
      }
    } else if (source.pxcor == world.maxPxcor()) {
      return source.fastGetPatchAt(world.minPxcor(), source.pycor + 1);
    } else {
      return source.fastGetPatchAt(source.pxcor + 1, source.pycor + 1);
    }
  }

}
