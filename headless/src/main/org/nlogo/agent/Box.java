// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

import org.nlogo.api.AgentException;
import org.nlogo.api.AgentKindJ;
import org.nlogo.api.I18N;

final strictfp class Box
    extends Topology {
  Box(World world) {
    super(world);
  }

  //wrapping coordinates

  @Override
  double wrapX(double x)
      throws AgentException {
    double max = world.maxPxcor() + 0.5;
    double min = world.minPxcor() - 0.5;
    if (x >= max || x < min) {
      throw new AgentException(I18N.errorsJ().get("org.nlogo.agent.Box.cantMoveTurtleBeyondWorldEdge"));
    }
    return x;
  }

  @Override
  double wrapY(double y)
      throws AgentException {
    double max = world.maxPycor() + 0.5;
    double min = world.minPycor() - 0.5;
    if (y >= max || y < min) {
      throw new AgentException(I18N.errorsJ().get("org.nlogo.agent.Box.cantMoveTurtleBeyondWorldEdge"));
    }
    return y;
  }

  @Override
  double distanceWrap(double dx, double dy, double x1, double y1, double x2, double y2) {
    return world.rootsTable.gridRoot(dx * dx + dy * dy);
  }

  @Override
  double towardsWrap(double headingX, double headingY) {
    if (headingX == 0) {
      return headingY > 0 ? 0 : 180;
    }
    if (headingY == 0) {
      return headingX > 0 ? 90 : 270;
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

    if ((xc > world.maxPxcor() + 0.5) ||
        (xc < world.minPxcor() - 0.5)) {
      return null;
    }
    return world.getPatchAt(xc, yc);
  }

  @Override
  double shortestPathX(double x1, double x2) {
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

    for (y = 0; y < yy; y++) {
      for (x = 0; x < xx; x++) {

        double diffuseVal = (scratch[x][y] / 8) * diffuseparam;
        if (y > 0 && y < yy - 1 && x > 0 && x < xx - 1) {
          scratch2[x][y] += scratch[x][y] - (8 * diffuseVal);
          scratch2[x - 1][y - 1] += diffuseVal;
          scratch2[x - 1][y] += diffuseVal;
          scratch2[x - 1][y + 1] += diffuseVal;
          scratch2[x][y + 1] += diffuseVal;
          scratch2[x][y - 1] += diffuseVal;
          scratch2[x + 1][y - 1] += diffuseVal;
          scratch2[x + 1][y] += diffuseVal;
          scratch2[x + 1][y + 1] += diffuseVal;
        } else if (y > 0 && y < yy - 1) {
          if (x == 0) {
            scratch2[x][y] += scratch[x][y] - (5 * diffuseVal);
            scratch2[x][y + 1] += diffuseVal;
            scratch2[x][y - 1] += diffuseVal;
            scratch2[x + 1][y - 1] += diffuseVal;
            scratch2[x + 1][y] += diffuseVal;
            scratch2[x + 1][y + 1] += diffuseVal;
          } else {
            scratch2[x][y] += scratch[x][y] - (5 * diffuseVal);
            scratch2[x][y + 1] += diffuseVal;
            scratch2[x][y - 1] += diffuseVal;
            scratch2[x - 1][y - 1] += diffuseVal;
            scratch2[x - 1][y] += diffuseVal;
            scratch2[x - 1][y + 1] += diffuseVal;
          }
        } else if (x > 0 && x < xx - 1) {
          if (y == 0) {
            scratch2[x][y] += scratch[x][y] - (5 * diffuseVal);
            scratch2[x - 1][y] += diffuseVal;
            scratch2[x - 1][y + 1] += diffuseVal;
            scratch2[x][y + 1] += diffuseVal;
            scratch2[x + 1][y] += diffuseVal;
            scratch2[x + 1][y + 1] += diffuseVal;
          } else {
            scratch2[x][y] += scratch[x][y] - (5 * diffuseVal);
            scratch2[x - 1][y] += diffuseVal;
            scratch2[x - 1][y - 1] += diffuseVal;
            scratch2[x][y - 1] += diffuseVal;
            scratch2[x + 1][y] += diffuseVal;
            scratch2[x + 1][y - 1] += diffuseVal;
          }
        } else if (x == 0) {
          if (y == 0) {
            scratch2[x][y] += scratch[x][y] - (3 * diffuseVal);
            scratch2[x][y + 1] += diffuseVal;
            scratch2[x + 1][y] += diffuseVal;
            scratch2[x + 1][y + 1] += diffuseVal;
          } else {
            scratch2[x][y] += scratch[x][y] - (3 * diffuseVal);
            scratch2[x][y - 1] += diffuseVal;
            scratch2[x + 1][y] += diffuseVal;
            scratch2[x + 1][y - 1] += diffuseVal;
          }
        } else {
          if (y == 0) {
            scratch2[x][y] += scratch[x][y] - (3 * diffuseVal);
            scratch2[x][y + 1] += diffuseVal;
            scratch2[x - 1][y] += diffuseVal;
            scratch2[x - 1][y + 1] += diffuseVal;
          } else {
            scratch2[x][y] += scratch[x][y] - (3 * diffuseVal);
            scratch2[x][y - 1] += diffuseVal;
            scratch2[x - 1][y] += diffuseVal;
            scratch2[x - 1][y - 1] += diffuseVal;
          }
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

    for (y = 0; y < yy; y++) {
      for (x = 0; x < xx; x++) {

        double diffuseVal = (scratch[x][y] / 4) * diffuseparam;

        if (y > 0 && y < yy - 1 && x > 0 && x < xx - 1) {
          scratch2[x][y] += scratch[x][y] - (4 * diffuseVal);
          scratch2[x - 1][y] += diffuseVal;
          scratch2[x][y + 1] += diffuseVal;
          scratch2[x][y - 1] += diffuseVal;
          scratch2[x + 1][y] += diffuseVal;
        } else if (y > 0 && y < yy - 1) {
          if (x == 0) {
            scratch2[x][y] += scratch[x][y] - (3 * diffuseVal);
            scratch2[x][y + 1] += diffuseVal;
            scratch2[x][y - 1] += diffuseVal;
            scratch2[x + 1][y] += diffuseVal;
          } else {
            scratch2[x][y] += scratch[x][y] - (3 * diffuseVal);
            scratch2[x][y + 1] += diffuseVal;
            scratch2[x][y - 1] += diffuseVal;
            scratch2[x - 1][y] += diffuseVal;
          }
        } else if (x > 0 && x < xx - 1) {
          if (y == 0) {
            scratch2[x][y] += scratch[x][y] - (3 * diffuseVal);
            scratch2[x - 1][y] += diffuseVal;
            scratch2[x][y + 1] += diffuseVal;
            scratch2[x + 1][y] += diffuseVal;
          } else {
            scratch2[x][y] += scratch[x][y] - (3 * diffuseVal);
            scratch2[x - 1][y] += diffuseVal;
            scratch2[x][y - 1] += diffuseVal;
            scratch2[x + 1][y] += diffuseVal;
          }
        } else if (x == 0) {
          if (y == 0) {
            scratch2[x][y] += scratch[x][y] - (2 * diffuseVal);
            scratch2[x][y + 1] += diffuseVal;
            scratch2[x + 1][y] += diffuseVal;
          } else {
            scratch2[x][y] += scratch[x][y] - (2 * diffuseVal);
            scratch2[x][y - 1] += diffuseVal;
            scratch2[x + 1][y] += diffuseVal;
          }
        } else {
          if (y == 0) {
            scratch2[x][y] += scratch[x][y] - (2 * diffuseVal);
            scratch2[x][y + 1] += diffuseVal;
            scratch2[x - 1][y] += diffuseVal;
          } else {
            scratch2[x][y] += scratch[x][y] - (2 * diffuseVal);
            scratch2[x][y - 1] += diffuseVal;
            scratch2[x - 1][y] += diffuseVal;
          }
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
  double observerY() {
    return 0.0;
  }

  @Override
  double followOffsetX() {
    return 0.0;
  }

  @Override
  double followOffsetY() {
    return 0.0;
  }

  @Override
  AgentSet getNeighbors(Patch source) {
    //added to reduce method calls (dont know if it is neccessary or not)
    double xLoc = source.pxcor;
    double yLoc = source.pycor;

    // special cases when we only have one patch in the world
    if (xLoc == world._maxPxcor && xLoc == world._minPxcor &&
        yLoc == world._maxPycor && yLoc == world._minPycor) {
      return world.noPatches();
    }

    if (xLoc == world.maxPxcor()) {
      if (xLoc == world.minPxcor()) {
        if (yLoc == world.maxPycor()) {
          return AgentSet.fromArray(AgentKindJ.Patch(),
                                   new Agent[]{getPatchSouth(source)});
        } else if (yLoc == world.minPycor()) {
          return AgentSet.fromArray(AgentKindJ.Patch(),
                                   new Agent[]{getPatchNorth(source)});
        } else {
          return AgentSet.fromArray(AgentKindJ.Patch(),
                                   new Agent[]{getPatchNorth(source),
                                               getPatchSouth(source)});
        }
      } else {
        if (yLoc == world.maxPycor()) {
          if (yLoc == world.minPycor()) {
            return AgentSet.fromArray(AgentKindJ.Patch(),
                                     new Agent[]{
                                       getPatchWest(source)});
          } else {
            return AgentSet.fromArray(AgentKindJ.Patch(),
                                     new Agent[]{
                                       getPatchSouth(source),
                                       getPatchWest(source),
                                       getPatchSouthWest(source)});
          }
        } else if (yLoc == world.minPycor()) {
          return AgentSet.fromArray(AgentKindJ.Patch(),
                                   new Agent[]{
                                     getPatchNorth(source),
                                     getPatchWest(source),
                                     getPatchNorthWest(source)});
        } else {
          return AgentSet.fromArray(AgentKindJ.Patch(),
                                   new Agent[]{
                                     getPatchNorth(source),
                                     getPatchSouth(source),
                                     getPatchWest(source),
                                     getPatchSouthWest(source),
                                     getPatchNorthWest(source)});
        }
      }
    } else if (xLoc == world.minPxcor()) {
      if (yLoc == world.maxPycor()) {
        if (yLoc == world.minPycor()) {
          return AgentSet.fromArray(AgentKindJ.Patch(),
                                   new Agent[]{getPatchEast(source)});
        } else {
          return AgentSet.fromArray(AgentKindJ.Patch(),
              new Agent[]{getPatchEast(source),
                          getPatchSouth(source),
                          getPatchSouthEast(source)});
        }
      } else if (yLoc == world.minPycor()) {
        return AgentSet.fromArray(AgentKindJ.Patch(),
                                 new Agent[]{getPatchNorth(source),
                                             getPatchEast(source),
                                             getPatchNorthEast(source)});
      } else {
        return AgentSet.fromArray(AgentKindJ.Patch(),
                                 new Agent[]{
                                   getPatchNorth(source),
                                   getPatchEast(source),
                                   getPatchSouth(source),
                                   getPatchNorthEast(source),
                                   getPatchSouthEast(source)});
      }
    } else if (yLoc == world.maxPycor()) {
      if (yLoc == world.minPycor()) {
        return AgentSet.fromArray(AgentKindJ.Patch(),
                                 new Agent[]{
                                   getPatchEast(source),
                                   getPatchWest(source)});
      } else {
        return AgentSet.fromArray(AgentKindJ.Patch(),
            new Agent[]{getPatchEast(source), getPatchSouth(source),
                        getPatchWest(source), getPatchSouthEast(source),
                        getPatchSouthWest(source)});
      }
    } else if (yLoc == world.minPycor()) {
      return AgentSet.fromArray(AgentKindJ.Patch(),
                               new Agent[]{getPatchNorth(source), getPatchEast(source),
                                           getPatchWest(source), getPatchNorthEast(source),
                                           getPatchNorthWest(source)});
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
    //added to reduce method calls (dont know if it is neccessary or not)
    int xLoc = source.pxcor;
    int yLoc = source.pycor;

    if (xLoc == world.maxPxcor()) {
      if (xLoc == world.minPxcor()) {
        if (yLoc == world.maxPycor()) {
          if (yLoc == world.minPycor()) {
            return world.noPatches();
          } else {
            return AgentSet.fromArray(AgentKindJ.Patch(),
                                     new Agent[]{getPatchSouth(source)});
          }
        } else if (yLoc == world.minPycor()) {
          return AgentSet.fromArray(AgentKindJ.Patch(),
                                   new Agent[]{getPatchNorth(source)});
        } else {
          return AgentSet.fromArray(AgentKindJ.Patch(),
                                   new Agent[]{getPatchNorth(source),
                                               getPatchSouth(source)});
        }
      } else {
        if (yLoc == world.maxPycor()) {
          if (yLoc == world.minPycor()) {
            return AgentSet.fromArray(AgentKindJ.Patch(),
                                     new Agent[]{getPatchWest(source)});
          } else {
            return AgentSet.fromArray(AgentKindJ.Patch(),
                                     new Agent[]{getPatchSouth(source),
                                                 getPatchWest(source)});
          }
        } else if (yLoc == world.minPycor()) {
          return AgentSet.fromArray(AgentKindJ.Patch(),
                                   new Agent[]{getPatchNorth(source),
                                               getPatchWest(source)});
        } else {
          return AgentSet.fromArray(AgentKindJ.Patch(),
                                   new Agent[]{getPatchNorth(source),
                                               getPatchSouth(source),
                                               getPatchWest(source)});
        }
      }
    } else if (xLoc == world.minPxcor()) {
      if (yLoc == world.maxPycor()) {
        if (yLoc == world.minPycor()) {
          return AgentSet.fromArray(AgentKindJ.Patch(),
                                   new Agent[]{getPatchEast(source)});
        } else {
          return AgentSet.fromArray(AgentKindJ.Patch(),
                                   new Agent[]{getPatchEast(source),
                                               getPatchSouth(source)});
        }
      } else if (yLoc == world.minPycor()) {
        return AgentSet.fromArray(AgentKindJ.Patch(),
                                 new Agent[]{getPatchNorth(source),
                                             getPatchEast(source)});
      } else {
        return AgentSet.fromArray(AgentKindJ.Patch(),
                                 new Agent[]{getPatchNorth(source),
                                             getPatchEast(source),
                                             getPatchSouth(source)});
      }
    } else if (yLoc == world.maxPycor()) {
      if (yLoc == world.minPycor()) {
        return AgentSet.fromArray(AgentKindJ.Patch(),
                                 new Agent[]{getPatchEast(source),
                                             getPatchWest(source)});
      } else {
        return AgentSet.fromArray(AgentKindJ.Patch(),
                                 new Agent[]{getPatchEast(source),
                                             getPatchSouth(source),
                                             getPatchWest(source)});
      }
    } else if (yLoc == world.minPycor()) {
      return AgentSet.fromArray(AgentKindJ.Patch(),
          new Agent[]{getPatchNorth(source),
                      getPatchEast(source),
                      getPatchWest(source)});
    } else {
      return AgentSet.fromArray(AgentKindJ.Patch(),
                               new Agent[]{getPatchNorth(source), getPatchEast(source),
                                           getPatchSouth(source), getPatchWest(source)});
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
    if (source.pxcor == world.maxPxcor()) {
      return null;
    }
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
    if (source.pxcor == world.minPxcor()) {
      return null;
    }
    return getPatchWest(source);
  }

  @Override
  Patch getPNE(Patch source) {
    if (source.pxcor == world.maxPxcor() ||
        source.pycor == world.maxPycor()) {
      return null;
    }
    return getPatchNorthEast(source);
  }

  @Override
  Patch getPSE(Patch source) {
    if (source.pxcor == world.maxPxcor() ||
        source.pycor == world.minPycor()) {
      return null;
    }
    return getPatchSouthEast(source);
  }

  @Override
  Patch getPSW(Patch source) {
    if (source.pxcor == world.minPxcor() ||
        source.pycor == world.minPycor()) {
      return null;
    }
    return getPatchSouthWest(source);
  }

  @Override
  Patch getPNW(Patch source) {
    if (source.pxcor == world.minPxcor() ||
        source.pycor == world.maxPycor()) {
      return null;
    }
    return getPatchNorthWest(source);
  }
}
