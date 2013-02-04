// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

import org.nlogo.api.AgentException;
import org.nlogo.api.AgentKindJ;

//world wraps along y-axis but not x-axis
final strictfp class HorizCylinder
    extends Topology {
  HorizCylinder(World world) {
    super(world);
  }

  //wrapping coordinates

  @Override
  double wrapX(double x)
      throws AgentException {
    double max = world.maxPxcor() + 0.5;
    double min = world.minPxcor() - 0.5;
    if (x >= max || x < min) {
      throw new AgentException("Cannot move turtle beyond the world's edge.");
    }
    return x;
  }

  @Override
  double wrapY(double y) {
    return wrap(y, world.minPycor() - 0.5, world.maxPycor() + 0.5);
  }

  @Override
  double distanceWrap(double dx, double dy, double x1, double y1, double x2, double y2) {
    double dy2 = y1 > y2 ? (y2 + world.worldHeight()) - y1 :
        (y2 - world.worldHeight()) - y1;
    dy = StrictMath.abs(dy2) < StrictMath.abs(dy) ? dy2 : dy;

    return world.rootsTable.gridRoot(dx * dx + dy * dy);
  }

  @Override
  double towardsWrap(double headingX, double headingY) {
    headingY = wrap(headingY, (-(double) world.worldHeight() / 2.0),
        (world.worldHeight() / 2.0));

    if (headingY == 0) {
      return headingX > 0 ? 90 : 270;
    }
    if (headingX == 0) {
      return headingY > 0 ? 0 : 180;
    }

    return (270 + StrictMath.toDegrees
        (StrictMath.PI + StrictMath.atan2(-headingY, headingX)))
        % 360;
  }

  @Override
  Patch getPatchAt(double xc, double yc)
      throws AgentException {
    if ((xc > world.maxPxcor() + 0.5) ||
        (xc < world.minPxcor() - 0.5)) {
      return null;
    } else {
      return world.getPatchAt(xc, yc);
    }
  }

  @Override
  double shortestPathX(double x1, double x2) {
    return x2;
  }

  @Override
  double shortestPathY(double y1, double y2) {
    double yprime;

    if (y1 > y2) {
      yprime = y2 + world.worldHeight();
    } else {
      yprime = y2 - world.worldHeight();
    }

    if (StrictMath.abs(y2 - y1) > StrictMath.abs(yprime - y1)) {
      y2 = yprime;
    }

    return y2;
  }

  @Override
  void diffuse(double diffuseparam, int vn)
      throws AgentException, PatchException {
    int xx = world.worldWidth();
    int yy = world.worldHeight();
    int xx2 = xx * 2;
    int yy2 = yy * 2;
    double[][] scratch = world.getPatchScratch();
    double[][] scratch2 = new double[xx][yy];
    int x = 0, y = 0;
    int minx = world.minPxcor();
    int miny = world.minPycor();

    try {
      for (y = 0; y < yy; y++) {
        for (x = 0; x < xx; x++) {
          scratch[x][y] =
              ((Double) world.fastGetPatchAt(x + minx, y + miny)
                  .getPatchVariable(vn))
                  .doubleValue();
          scratch2[x][y] = 0;
        }
      }
    } catch (ClassCastException ex) {
      throw new PatchException(world.fastGetPatchAt
          ((int) wrapX(x), (int) wrapY(y)));
    }

    for (y = yy; y < yy2; y++) {
      for (x = xx; x < xx2; x++) {

        double diffuseVal = (scratch[x - xx][y - yy] / 8) * diffuseparam;

        if (x > xx && x < xx2 - 1) {
          scratch2[x - xx][y - yy] += scratch[x - xx][y - yy] - (8 * diffuseVal);
          scratch2[(x - 1) % xx][(y - 1) % yy] += diffuseVal;
          scratch2[(x - 1) % xx][y % yy] += diffuseVal;
          scratch2[(x - 1) % xx][(y + 1) % yy] += diffuseVal;
          scratch2[x % xx][(y + 1) % yy] += diffuseVal;
          scratch2[x % xx][(y - 1) % yy] += diffuseVal;
          scratch2[(x + 1) % xx][(y - 1) % yy] += diffuseVal;
          scratch2[(x + 1) % xx][y % yy] += diffuseVal;
          scratch2[(x + 1) % xx][(y + 1) % yy] += diffuseVal;
        } else if (x == xx) {
          scratch2[x - xx][y - yy] += scratch[x - xx][y - yy] - (5 * diffuseVal);
          scratch2[x % xx][(y + 1) % yy] += diffuseVal;
          scratch2[x % xx][(y - 1) % yy] += diffuseVal;
          scratch2[(x + 1) % xx][(y - 1) % yy] += diffuseVal;
          scratch2[(x + 1) % xx][y % yy] += diffuseVal;
          scratch2[(x + 1) % xx][(y + 1) % yy] += diffuseVal;
        } else {
          scratch2[x - xx][y - yy] += scratch[x - xx][y - yy] - (5 * diffuseVal);
          scratch2[x % xx][(y + 1) % yy] += diffuseVal;
          scratch2[x % xx][(y - 1) % yy] += diffuseVal;
          scratch2[(x - 1) % xx][(y - 1) % yy] += diffuseVal;
          scratch2[(x - 1) % xx][y % yy] += diffuseVal;
          scratch2[(x - 1) % xx][(y + 1) % yy] += diffuseVal;
        }
      }
    }

    for (y = 0; y < yy; y++) {
      for (x = 0; x < xx; x++) {
        if (scratch2[x][y] != scratch[x][y]) {
          world.getPatchAtWrap(x + minx, y + miny)
              .setPatchVariable(vn, Double.valueOf(scratch2[x][y]));
        }
      }
    }

  }

  @Override
  void diffuse4(double diffuseparam, int vn)
      throws AgentException, PatchException {
    int xx = world.worldWidth();
    int yy = world.worldHeight();
    int xx2 = xx * 2;
    int yy2 = yy * 2;
    double[][] scratch = world.getPatchScratch();
    double[][] scratch2 = new double[xx][yy];
    int x = 0, y = 0;
    int minx = world.minPxcor();
    int miny = world.minPycor();

    try {
      for (y = 0; y < yy; y++) {
        for (x = 0; x < xx; x++) {
          scratch[x][y] =
              ((Double) world.fastGetPatchAt(x + minx, y + miny)
                  .getPatchVariable(vn))
                  .doubleValue();
          scratch2[x][y] = 0;
        }
      }
    } catch (ClassCastException ex) {
      throw new PatchException(world.fastGetPatchAt
          ((int) wrapX(x), (int) wrapY(y)));
    }

    for (y = yy; y < yy2; y++) {
      for (x = xx; x < xx2; x++) {

        double diffuseVal = (scratch[x - xx][y - yy] / 4) * diffuseparam;

        if (x > 0 && x < xx - 1) {
          scratch2[x - xx][y - yy] += scratch[x - xx][y - yy] - (4 * diffuseVal);
          scratch2[(x - 1) % xx][y] += diffuseVal;
          scratch2[x % xx][(y + 1) % yy] += diffuseVal;
          scratch2[x % xx][(y - 1) % yy] += diffuseVal;
          scratch2[(x + 1) % xx][y % yy] += diffuseVal;
        } else if (x == xx) {
          scratch2[x - xx][y - yy] += scratch[x - xx][y - yy] - (3 * diffuseVal);
          scratch2[x % xx][(y + 1) % yy] += diffuseVal;
          scratch2[x % xx][(y - 1) % yy] += diffuseVal;
          scratch2[(x + 1) % xx][y % yy] += diffuseVal;
        } else {
          scratch2[x - xx][y - yy] += scratch[x - xx][y - yy] - (3 * diffuseVal);
          scratch2[x % xx][(y + 1) % yy] += diffuseVal;
          scratch2[x % xx][(y - 1) % yy] += diffuseVal;
          scratch2[(x - 1) % xx][y % yy] += diffuseVal;
        }
      }
    }

    for (y = 0; y < yy; y++) {
      for (x = 0; x < xx; x++) {
        if (scratch2[x][y] != scratch[x][y]) {
          world.getPatchAtWrap(x + minx, y + miny)
              .setPatchVariable(vn, Double.valueOf(scratch2[x][y]));
        }
      }
    }

  }

  @Override
  double observerX() {
    return 0.0;
  }

  @Override
  double followOffsetX() {
    return 0.0;
  }

  @Override
  AgentSet getNeighbors(Patch source) {
    if (source.pxcor == world.maxPxcor()) {
      if (source.pxcor == world.minPxcor()) {
        if (source.pycor == world.maxPycor() && source.pycor == world.minPycor()) {
          return world.noPatches();
        } else {
          return AgentSet.fromArray(AgentKindJ.Patch(),
                                   new Agent[]{getPatchNorth(source),
                                               getPatchSouth(source)});
        }
      } else {
        if (source.pycor == world.maxPycor() && source.pycor == world.minPycor()) {
          return AgentSet.fromArray(AgentKindJ.Patch(),
                                   new Agent[]{getPatchWest(source)});
        } else {
          return AgentSet.fromArray(AgentKindJ.Patch(),
              new Agent[]{getPatchNorth(source), getPatchSouth(source),
                          getPatchWest(source), getPatchSouthWest(source),
                          getPatchNorthWest(source)});
        }
      }
    } else if (source.pxcor == world.minPxcor()) {
      if (source.pycor == world.maxPycor() && source.pycor == world.minPycor()) {
        return AgentSet.fromArray(AgentKindJ.Patch(),
                                 new Agent[]{getPatchEast(source)});
      } else {
        return AgentSet.fromArray(AgentKindJ.Patch(),
            new Agent[]{getPatchNorth(source), getPatchEast(source),
                getPatchSouth(source), getPatchNorthEast(source),
                        getPatchSouthEast(source)});
      }
    } else {
      if (source.pycor == world.maxPycor() && source.pycor == world.minPycor()) {
        return AgentSet.fromArray(AgentKindJ.Patch(),
                                 new Agent[]{getPatchEast(source),
                                             getPatchWest(source)});
      } else {
        return AgentSet.fromArray(AgentKindJ.Patch(),
            new Agent[]{getPatchNorth(source), getPatchEast(source),
                        getPatchSouth(source), getPatchWest(source),
                        getPatchNorthEast(source), getPatchSouthEast(source),
                        getPatchSouthWest(source), getPatchNorthWest(source)});
      }
    }
  }

  @Override
  AgentSet getNeighbors4(Patch source) {
    if (source.pxcor == world.maxPxcor()) {
      if (source.pxcor == world.minPxcor()) {
        if (source.pycor == world.maxPycor() && source.pycor == world.minPycor()) {
          return world.noPatches();
        } else {
          return AgentSet.fromArray(AgentKindJ.Patch(),
              new Agent[]{getPatchNorth(source),
                          getPatchSouth(source)});
        }
      } else {
        if (source.pycor == world.maxPycor() && source.pycor == world.minPycor()) {
          return AgentSet.fromArray(AgentKindJ.Patch(),
                                   new Agent[]{getPatchWest(source)});
        } else {
          return AgentSet.fromArray(AgentKindJ.Patch(),
                                   new Agent[]{getPatchNorth(source),
                                               getPatchSouth(source),
                                               getPatchWest(source)});
        }
      }
    } else if (source.pxcor == world.minPxcor()) {
      if (source.pycor == world.maxPycor() && source.pycor == world.minPycor()) {
        return AgentSet.fromArray(AgentKindJ.Patch(),
                                 new Agent[]{getPatchEast(source)});
      } else {
        return AgentSet.fromArray(AgentKindJ.Patch(),
                                 new Agent[]{getPatchNorth(source),
                                             getPatchEast(source),
                                             getPatchSouth(source)});
      }
    } else {
      if (source.pycor == world.maxPycor() && source.pycor == world.minPycor()) {
        return AgentSet.fromArray(AgentKindJ.Patch(),
                                 new Agent[]{getPatchEast(source),
                                             getPatchWest(source)});
      } else {
        return AgentSet.fromArray(AgentKindJ.Patch(),
            new Agent[]{getPatchNorth(source), getPatchEast(source),
                        getPatchSouth(source), getPatchWest(source)});
      }
    }
  }

  //get patch

  @Override
  Patch getPN(Patch source) {
    return getPatchNorth(source);
  }

  @Override
  Patch getPE(Patch source) {
    if (source.pxcor == world.maxPxcor()) {
      return null;
    }

    return getPatchEast(source);
  }

  @Override
  Patch getPS(Patch source) {
    return getPatchSouth(source);
  }

  @Override
  Patch getPW(Patch source) {
    if (source.pxcor == world.minPxcor()) {
      return null;
    }

    return getPatchWest(source);
  }

  @Override
  Patch getPNE(Patch source) {
    if (source.pxcor == world.maxPxcor()) {
      return null;
    }

    return getPatchNorthEast(source);
  }

  @Override
  Patch getPSE(Patch source) {
    if (source.pxcor == world.maxPxcor()) {
      return null;
    }

    return getPatchSouthEast(source);
  }

  @Override
  Patch getPSW(Patch source) {
    if (source.pxcor == world.minPxcor()) {
      return null;
    }

    return getPatchSouthWest(source);
  }

  @Override
  Patch getPNW(Patch source) {
    if (source.pxcor == world.minPxcor()) {
      return null;
    }

    return getPatchNorthWest(source);
  }
}
