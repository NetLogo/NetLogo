// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

import org.nlogo.api.AgentException;
import org.nlogo.api.AgentKindJ;

strictfp class Torus
    extends Topology {
  Torus(World world) {
    super(world);
  }

  //wrapping coordinates

  @Override
  double wrapX(double x) {
    return wrap(x, world.minPxcor() - 0.5, world.maxPxcor() + 0.5);
  }

  @Override
  double wrapY(double y) {
    return wrap(y, world.minPycor() - 0.5, world.maxPycor() + 0.5);
  }

  @Override
  double distanceWrap(double dx, double dy, double x1, double y1, double x2, double y2) {
    double dx2 = x1 > x2 ? (x2 + world.worldWidth()) - x1 :
        (x2 - world.worldWidth()) - x1;
    dx = StrictMath.abs(dx2) < StrictMath.abs(dx) ? dx2 : dx;

    double dy2 = y1 > y2 ? (y2 + world.worldHeight()) - y1 :
        (y2 - world.worldHeight()) - y1;
    dy = StrictMath.abs(dy2) < StrictMath.abs(dy) ? dy2 : dy;

    return world.rootsTable.gridRoot(dx * dx + dy * dy);
  }

  @Override
  double towardsWrap(double headingX, double headingY) {
    headingX = wrap(headingX, (-(double) world.worldWidth() / 2.0),
        (world.worldWidth() / 2.0));

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
    return world.getPatchAt(xc, yc);
  }

  @Override
  AgentSet getNeighbors(Patch source) {
    if (source.pxcor == world.maxPxcor() && source.pxcor == world.minPxcor()) {
      if (source.pycor == world.maxPycor() && source.pycor == world.minPycor()) {
        return world.noPatches();
      } else {
        return AgentSet.fromArray
          (AgentKindJ.Patch(), new Agent[]{getPatchNorth(source),
                                           getPatchSouth(source)});
      }
    } else if (source.pycor == world.maxPycor() && source.pycor == world.minPycor()) {
      return AgentSet.fromArray
        (AgentKindJ.Patch(),
              new Agent[]{getPatchEast(source), getPatchWest(source)});
    } else {
      return AgentSet.fromArray(AgentKindJ.Patch(),
          new Agent[]{getPatchNorth(source), getPatchEast(source),
                      getPatchSouth(source), getPatchWest(source),
                      getPatchNorthEast(source), getPatchSouthEast(source),
                      getPatchSouthWest(source), getPatchNorthWest(source)});
    }
  }

  @Override
  AgentSet getNeighbors4(Patch source) {
    if (source.pxcor == world.maxPxcor() && source.pxcor == world.minPxcor()) {
      if (source.pycor == world.maxPycor() && source.pycor == world.minPycor()) {
        return world.noPatches();
      } else {
        return AgentSet.fromArray
          (AgentKindJ.Patch(),
           new Agent[]{getPatchNorth(source), getPatchSouth(source)});
      }
    } else if (source.pycor == world.maxPycor() && source.pycor == world.minPycor()) {
      return AgentSet.fromArray
        (AgentKindJ.Patch(),
              new Agent[]{getPatchEast(source), getPatchWest(source)});
    } else {
      return AgentSet.fromArray(AgentKindJ.Patch(),
          new Agent[]{getPatchNorth(source), getPatchEast(source),
                      getPatchSouth(source), getPatchWest(source)});
    }
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
    int xx2 = xx * 2;
    int yy = world.worldHeight();
    int yy2 = yy * 2;
    double[][] scratch = world.getPatchScratch();
    int x = 0, y = 0;

    try {
      for (y = 0; y < yy; y++) {
        for (x = 0; x < xx; x++) {
          scratch[x][y] =
              ((Double) world.fastGetPatchAt((int) wrapX(x), (int) wrapY(y))
                  .getPatchVariable(vn))
                  .doubleValue();
        }
      }
    } catch (ClassCastException ex) {
      throw new PatchException(world.fastGetPatchAt
          ((int) wrapX(x), (int) wrapY(y)));
    }

    for (y = yy; y < yy2; y++) {
      for (x = xx; x < xx2; x++) {
        double sum;
        sum = scratch[(x - 1) % xx][(y - 1) % yy];
        sum += scratch[(x - 1) % xx][(y) % yy];
        sum += scratch[(x - 1) % xx][(y + 1) % yy];
        sum += scratch[(x) % xx][(y - 1) % yy];
        sum += scratch[(x) % xx][(y + 1) % yy];
        sum += scratch[(x + 1) % xx][(y - 1) % yy];
        sum += scratch[(x + 1) % xx][(y) % yy];
        sum += scratch[(x + 1) % xx][(y + 1) % yy];
        double oldval = scratch[x - xx][y - yy];
        double newval =
            oldval * (1.0 - diffuseparam)
                + (sum / 8) * diffuseparam;
        if (newval != oldval) {
          world.getPatchAt(x - xx, y - yy)
              .setPatchVariable(vn, Double.valueOf(newval));
        }
      }
    }
  }

  @Override
  void diffuse4(double diffuseparam, int vn)
      throws AgentException, PatchException {
    int xx = world.worldWidth();
    int yy = world.worldHeight();
    double[][] scratch = world.getPatchScratch();

    int x = 0, y = 0;
    try {
      for (y = 0; y < yy; y++) {
        for (x = 0; x < xx; x++) {
          scratch[x][y] =
              ((Double) world.fastGetPatchAt((int) wrapX(x),
                  (int) wrapY(y))
                  .getPatchVariable(vn))
                  .doubleValue();
        }
      }
    } catch (ClassCastException ex) {
      throw new PatchException(world.fastGetPatchAt((int) wrapX(x),
          (int) wrapY(y)));
    }

    for (y = 0; y < yy; y++) {
      for (x = 0; x < xx; x++) {
        double sum = 0;
        sum += scratch[(x + xx - 1) % xx][(y + yy) % yy];  // left patch
        sum += scratch[(x + xx) % xx][(y + yy + 1) % yy];  // top patch
        sum += scratch[(x + xx + 1) % xx][(y + yy) % yy];  // right patch
        sum += scratch[(x + xx) % xx][(y + yy - 1) % yy];  // bottom patch

        double newval = scratch[x][y] * (1 - diffuseparam) + sum * diffuseparam / 4;
        if (newval != scratch[x][y]) {
          world.getPatchAt(x, y).setPatchVariable(vn, Double.valueOf(newval));
        }
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
    return getPatchEast(source);
  }

  @Override
  Patch getPS(Patch source) {
    return getPatchSouth(source);
  }

  @Override
  Patch getPW(Patch source) {
    return getPatchWest(source);
  }

  @Override
  Patch getPNE(Patch source) {
    return getPatchNorthEast(source);
  }

  @Override
  Patch getPSE(Patch source) {
    return getPatchSouthEast(source);
  }

  @Override
  Patch getPSW(Patch source) {
    return getPatchSouthWest(source);
  }

  @Override
  Patch getPNW(Patch source) {
    return getPatchNorthWest(source);
  }
}
