// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

import org.nlogo.api.AgentException;
import org.nlogo.api.AgentKindJ;

//world wraps along x-axis but not y-axis
final strictfp class VertCylinder
    extends Topology {
  VertCylinder(World world) {
    super(world);
  }


  //wrapping coordinates

  @Override
  double wrapX(double x) {
    return wrap(x, world.minPxcor() - 0.5, world.maxPxcor() + 0.5);
  }

  @Override
  double wrapY(double y)
      throws AgentException {
    double max = world.maxPycor() + 0.5;
    double min = world.minPycor() - 0.5;
    if (y >= max || y < min) {
      throw new AgentException("Cannot move turtle beyond the world's edge.");
    }
    return y;
  }

  @Override
  double distanceWrap(double dx, double dy, double x1, double y1, double x2, double y2) {
    double dx2 = x1 > x2 ? (x2 + world.worldWidth()) - x1 :
        (x2 - world.worldWidth()) - x1;
    dx = StrictMath.abs(dx2) < StrictMath.abs(dx) ? dx2 : dx;

    return world.rootsTable.gridRoot(dx * dx + dy * dy);
  }

  @Override
  double towardsWrap(double headingX, double headingY) {
    headingX = wrap(headingX, (-(double) world.worldWidth() / 2.0),
        (world.worldWidth() / 2.0));

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

    if ((yc > world.maxPycor() + 0.5) ||
        (yc < world.minPycor() - 0.5)) {
      return null;
    }

    return world.getPatchAt(xc, yc);
  }

  @Override
  double shortestPathX(double x1, double x2) {
    double xprime;

    if (x1 > x2) {
      xprime = x2 + world.worldWidth();
    } else {
      xprime = x2 - world.worldWidth();
    }

    if (StrictMath.abs(x2 - x1) > StrictMath.abs(xprime - x1)) {
      x2 = xprime;
    }

    return x2;
  }

  @Override
  double shortestPathY(double y1, double y2) {
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

        if (y > yy && y < yy2 - 1) {
          scratch2[x - xx][y - yy] += scratch[x - xx][y - yy] - (8 * diffuseVal);
          scratch2[(x - 1) % xx][(y - 1) % yy] += diffuseVal;
          scratch2[(x - 1) % xx][y % yy] += diffuseVal;
          scratch2[(x - 1) % xx][(y + 1) % yy] += diffuseVal;
          scratch2[x % xx][(y + 1) % yy] += diffuseVal;
          scratch2[x % xx][(y - 1) % yy] += diffuseVal;
          scratch2[(x + 1) % xx][(y - 1) % yy] += diffuseVal;
          scratch2[(x + 1) % xx][y % yy] += diffuseVal;
          scratch2[(x + 1) % xx][(y + 1) % yy] += diffuseVal;
        } else if (y == yy) {
          scratch2[x - xx][y - yy] += scratch[x - xx][y - yy] - (5 * diffuseVal);
          scratch2[(x - 1) % xx][y % yy] += diffuseVal;
          scratch2[(x - 1) % xx][(y + 1) % yy] += diffuseVal;
          scratch2[x % xx][(y + 1) % yy] += diffuseVal;
          scratch2[(x + 1) % xx][y % yy] += diffuseVal;
          scratch2[(x + 1) % xx][(y + 1) % yy] += diffuseVal;
        } else {
          scratch2[x - xx][y - yy] += scratch[x - xx][y - yy] - (5 * diffuseVal);
          scratch2[(x - 1) % xx][y % yy] += diffuseVal;
          scratch2[(x - 1) % xx][(y - 1) % yy] += diffuseVal;
          scratch2[x % xx][(y - 1) % yy] += diffuseVal;
          scratch2[(x + 1) % xx][y % yy] += diffuseVal;
          scratch2[(x + 1) % xx][(y - 1) % yy] += diffuseVal;
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

        if (y > yy && y < yy2 - 1) {
          scratch2[x - xx][y - yy] += scratch[x - xx][y - yy] - (4 * diffuseVal);
          scratch2[(x - 1) % xx][y % yy] += diffuseVal;
          scratch2[x % xx][(y + 1) % yy] += diffuseVal;
          scratch2[x % xx][(y - 1) % yy] += diffuseVal;
          scratch2[(x + 1) % xx][y % yy] += diffuseVal;
        } else if (y == yy) {
          scratch2[x - xx][y - yy] += scratch[x - xx][y - yy] - (3 * diffuseVal);
          scratch2[(x - 1) % xx][y % yy] += diffuseVal;
          scratch2[x % xx][(y + 1) % yy] += diffuseVal;
          scratch2[(x + 1) % xx][y % yy] += diffuseVal;
        } else {
          scratch2[x - xx][y - yy] += scratch[x - xx][y - yy] - (3 * diffuseVal);
          scratch2[(x - 1) % xx][y % yy] += diffuseVal;
          scratch2[x % xx][(y - 1) % yy] += diffuseVal;
          scratch2[(x + 1) % xx][y % yy] += diffuseVal;
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
  AgentSet getNeighbors(Patch source) {
    if (source.pycor == world.maxPycor()) {
      if (source.pycor == world.minPycor()) {
        if (source.pxcor == world.maxPxcor() && source.pxcor == world.minPxcor()) {
          return new ArrayAgentSet(AgentKindJ.Patch(),
              new Agent[]{},
              world);
        } else {
          return new ArrayAgentSet(AgentKindJ.Patch(),
              new Agent[]{getPatchEast(source),
                  getPatchWest(source)},
              world);
        }
      } else {
        if (source.pxcor == world.maxPxcor() && source.pxcor == world.minPxcor()) {
          return new ArrayAgentSet(AgentKindJ.Patch(),
              new Agent[]{getPatchSouth(source)},
              world);
        } else {
          return new ArrayAgentSet(AgentKindJ.Patch(),
              new Agent[]{getPatchEast(source), getPatchSouth(source),
                  getPatchWest(source), getPatchSouthEast(source),
                  getPatchSouthWest(source)},
              world);
        }
      }
    } else if (source.pycor == world.minPycor()) {
      if (source.pxcor == world.maxPxcor() && source.pxcor == world.minPxcor()) {
        return new ArrayAgentSet(AgentKindJ.Patch(),
            new Agent[]{getPatchNorth(source)},
            world);
      } else {
        return new ArrayAgentSet(AgentKindJ.Patch(),
            new Agent[]{getPatchNorth(source), getPatchEast(source),
                getPatchWest(source), getPatchNorthEast(source),
                getPatchNorthWest(source)},
            world);
      }
    } else {
      if (source.pxcor == world.maxPxcor() && source.pxcor == world.minPxcor()) {
        return new ArrayAgentSet(AgentKindJ.Patch(),
            new Agent[]{getPatchNorth(source), getPatchSouth(source)},
            world);
      } else {
        return new ArrayAgentSet(AgentKindJ.Patch(),
            new Agent[]{getPatchNorth(source), getPatchEast(source),
                getPatchSouth(source), getPatchWest(source),
                getPatchNorthEast(source), getPatchSouthEast(source),
                getPatchSouthWest(source), getPatchNorthWest(source)},
            world);
      }
    }
  }

  @Override
  double observerY() {
    return 0.0;
  }

  @Override
  double followOffsetY() {
    return 0.0;
  }

  @Override
  AgentSet getNeighbors4(Patch source) {
    if (source.pycor == world.maxPycor()) {
      if (source.pycor == world.minPycor()) {
        if (source.pxcor == world.maxPxcor() && source.pxcor == world.minPxcor()) {
          return new ArrayAgentSet(AgentKindJ.Patch(),
              new Agent[]{},
              world);
        } else {
          return new ArrayAgentSet(AgentKindJ.Patch(),
              new Agent[]{getPatchEast(source),
                  getPatchWest(source)},
              world);
        }
      } else {
        if (source.pxcor == world.maxPxcor() && source.pxcor == world.minPxcor()) {
          return new ArrayAgentSet(AgentKindJ.Patch(),
              new Agent[]{getPatchSouth(source)},
              world);
        } else {
          return new ArrayAgentSet(AgentKindJ.Patch(),
              new Agent[]{getPatchEast(source),
                  getPatchSouth(source),
                  getPatchWest(source)},
              world);
        }
      }
    } else if (source.pycor == world.minPycor()) {
      if (source.pxcor == world.maxPxcor() && source.pxcor == world.minPxcor()) {
        return new ArrayAgentSet(AgentKindJ.Patch(),
            new Agent[]{getPatchNorth(source)},
            world);
      } else {
        return new ArrayAgentSet(AgentKindJ.Patch(),
            new Agent[]{getPatchNorth(source),
                getPatchEast(source),
                getPatchWest(source)},
            world);
      }
    } else {
      if (source.pxcor == world.maxPxcor() && source.pxcor == world.minPxcor()) {
        return new ArrayAgentSet(AgentKindJ.Patch(),
            new Agent[]{getPatchNorth(source),
                getPatchSouth(source)},
            world);
      } else {
        return new ArrayAgentSet(AgentKindJ.Patch(),
            new Agent[]{getPatchNorth(source), getPatchEast(source),
                getPatchSouth(source), getPatchWest(source)},
            world);
      }
    }
  }

  //get patch

  @Override
  Patch getPN(Patch source) {
    if (source.pycor == world.maxPycor()) {
      return null;
    }

    return getPatchNorth(source);
  }

  @Override
  Patch getPE(Patch source) {
    return getPatchEast(source);
  }

  @Override
  Patch getPS(Patch source) {
    if (source.pycor == world.minPycor()) {
      return null;
    }

    return getPatchSouth(source);
  }

  @Override
  Patch getPW(Patch source) {
    return getPatchWest(source);
  }

  @Override
  Patch getPNE(Patch source) {
    if (source.pycor == world.maxPycor()) {
      return null;
    }

    return getPatchNorthEast(source);
  }

  @Override
  Patch getPSE(Patch source) {
    if (source.pycor == world.minPycor()) {
      return null;
    }

    return getPatchSouthEast(source);
  }

  @Override
  Patch getPSW(Patch source) {
    if (source.pycor == world.minPycor()) {
      return null;
    }

    return getPatchSouthWest(source);
  }

  @Override
  Patch getPNW(Patch source) {
    if (source.pycor == world.maxPycor()) {
      return null;
    }

    return getPatchNorthWest(source);
  }
}
