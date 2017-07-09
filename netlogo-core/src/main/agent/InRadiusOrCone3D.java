// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

import org.nlogo.api.AgentException;
import org.nlogo.api.Vect;
import org.nlogo.core.AgentKindJ;

import java.util.ArrayList;
import java.util.List;

public strictfp class InRadiusOrCone3D
  implements World.InRadiusOrCone {
  private final World3D world;

  InRadiusOrCone3D(World3D world) {
    this.world = world;
  }

  public List<Agent> inRadiusSimple(Agent agent, AgentSet sourceSet,
      double radius, boolean wrap) {
    return inRadius(agent, sourceSet, radius, wrap);
  }

  @Override
  public List<Agent> inRadius(Agent agent, AgentSet sourceSet,
                              double radius, boolean wrap) {
    int worldWidth = world.worldWidth();
    int worldHeight = world.worldHeight();
    int worldDepth = world.worldDepth();
    int maxPxcor = world.maxPxcor();
    int maxPycor = world.maxPycor();
    int minPxcor = world.minPxcor();
    int minPycor = world.minPycor();

    List<Agent> result = new ArrayList<Agent>();
    Patch3D startPatch;
    double startX, startY, startZ;
    if (agent instanceof Turtle) {
      Turtle3D startTurtle = (Turtle3D) agent;
      startPatch = (Patch3D) startTurtle.getPatchHere();
      startX = startTurtle.xcor();
      startY = startTurtle.ycor();
      startZ = startTurtle.zcor();
    } else {
      startPatch = (Patch3D) agent;
      startX = startPatch.pxcor;
      startY = startPatch.pycor;
      startZ = startPatch.pzcor;
    }

    int dxmin = 0;
    int dxmax = 0;
    int dymin = 0;
    int dymax = 0;
    int dzmin = 0;
    int dzmax = 0;

    int r = (int) StrictMath.ceil(radius);

    if (world.wrappingAllowedInX()) {
      double width = worldWidth / 2.0;
      if (r < width) {
        dxmax = r;
        dxmin = -r;
      } else {
        dxmax = (int) StrictMath.floor(width);
        dxmin = -(int) StrictMath.ceil(width - 1);
      }
    } else {
      int xdiff = minPxcor - startPatch.pxcor;
      dxmin = StrictMath.abs(xdiff) < r ? xdiff : -r;
      dxmax = StrictMath.min((maxPxcor - startPatch.pxcor), r);
    }
    if (world.wrappingAllowedInY()) {
      double height = worldHeight / 2.0;
      if (r < height) {
        dymax = r;
        dymin = -r;
      } else {
        dymax = (int) StrictMath.floor(height);
        dymin = -(int) StrictMath.ceil(height - 1);
      }
    } else {
      int ydiff = minPycor - startPatch.pycor;
      dymin = StrictMath.abs(ydiff) < r ? ydiff : -r;
      dymax = StrictMath.min((maxPycor - startPatch.pycor), r);
    }

    double depth = worldDepth / 2.0;
    if (r < depth) {
      dzmax = r;
      dzmin = -r;
    } else {
      dzmax = (int) StrictMath.floor(depth);
      dzmin = -(int) StrictMath.ceil(depth - 1);
    }

    Protractor3D protractor = world.protractor();

    for (int dz = dzmin; dz <= dzmax; dz++) {
      for (int dy = dymin; dy <= dymax; dy++) {
        for (int dx = dxmin; dx <= dxmax; dx++) {
          Patch3D patch = null;
          try {
            patch = startPatch.getPatchAtOffsets(dx, dy, dz);
          } catch (AgentException ex) {
            // at present wrapping is always on in 3D, so this can
            // never happen - ST 7/18/06
            throw new IllegalStateException(ex);
          }
          if (sourceSet.kind() == AgentKindJ.Patch()) {
            if (protractor.distance(patch.pxcor, patch.pycor, patch.pzcor,
                startX, startY, startZ,
                wrap)
                <= radius && (sourceSet == world.patches() || sourceSet.contains(patch))) {
              result.add(patch);
            }
          } else {
            // Only check patches that might have turtles within the radius on them.
            // The 1.415 (square root of 2) adjustment is necessary because it is
            // possible for portions of a patch to be within the circle even though
            // the center of the patch is outside the circle.
            if (StrictMath.sqrt(dx * dx + dy * dy + dz * dz) <= radius + 1.415) {
              for (Turtle turtle : patch.turtlesHere()) {
                if ((sourceSet == world.turtles() ||
                    // any turtle set with a non-null print name is either
                    // the set of all turtles, or a breed agentset - ST 2/19/04
                    (sourceSet.isBreedSet() && sourceSet == turtle.getBreed()) ||
                    (!sourceSet.isBreedSet() && sourceSet.contains(turtle))) &&
                    (protractor.distance(turtle.xcor(), turtle.ycor(),
                        ((Turtle3D) turtle).zcor(),
                        startX, startY, startZ, wrap)
                        <= radius)) {
                  result.add(turtle);
                }
              }
            }
          }
        }
      }
    }
    return result;
  }

  @Override
  public List<Agent> inCone(Turtle callingTurtle, AgentSet sourceSet,
                            double radius, double angle, boolean wrap) {
    int worldWidth = world.worldWidth();
    int worldHeight = world.worldHeight();
    int worldDepth = world.worldDepth();
    int maxPxcor = world.maxPxcor();
    int maxPycor = world.maxPycor();
    int minPxcor = world.minPxcor();
    int minPycor = world.minPycor();

    int m = 0;
    int n = 0;
    int k = 0;

    Turtle3D startTurtle = (Turtle3D) callingTurtle;

    if (wrap) {
      m = world.wrappingAllowedInX() ? (int) StrictMath.ceil(radius / worldWidth) : 0;
      n = world.wrappingAllowedInY() ? (int) StrictMath.ceil(radius / worldHeight) : 0;
      k = world.wrappingAllowedInZ() ? (int) StrictMath.ceil(radius / worldDepth) : 0;
    }

    List<Agent> result = new ArrayList<Agent>();
    Patch3D startPatch = (Patch3D) startTurtle.getPatchHere();
    double half = angle / 2;
    // dxmax and dymax determine which patches we will check.  the patches we
    // check always form a rectangle.  usually it will be a square, but if
    // the radius is large enough, then it might be a rectangle.

    int dxmin = 0;
    int dxmax = 0;
    int dymin = 0;
    int dymax = 0;
    int dzmin = 0;
    int dzmax = 0;

    int r = (int) StrictMath.ceil(radius);

    if (world.wrappingAllowedInX()) {
      double width = worldWidth / 2.0;
      if (r < width) {
        dxmax = r;
        dxmin = -r;
      } else {
        dxmax = (int) StrictMath.floor(width);
        dxmin = -(int) StrictMath.ceil(width - 1);
      }
    } else {
      int xdiff = minPxcor - startPatch.pxcor;
      dxmin = StrictMath.abs(xdiff) < r ? xdiff : -r;
      dxmax = StrictMath.min((maxPxcor - startPatch.pxcor), r);
    }
    if (world.wrappingAllowedInY()) {
      double height = worldHeight / 2.0;
      if (r < height) {
        dymax = r;
        dymin = -r;
      } else {
        dymax = (int) StrictMath.floor(height);
        dymin = -(int) StrictMath.ceil(height - 1);
      }
    } else {
      int ydiff = minPycor - startPatch.pycor;
      dymin = StrictMath.abs(ydiff) < r ? ydiff : -r;
      dymax = StrictMath.min((maxPycor - startPatch.pycor), r);
    }

    double depth = worldDepth / 2.0;
    if (r < depth) {
      dzmax = r;
      dzmin = -r;
    } else {
      dzmax = (int) StrictMath.floor(depth);
      dzmin = -(int) StrictMath.ceil(depth - 1);
    }

    // loop through the patches in the rectangle.  (it doesn't matter what
    // order we check them in.)
    for (int dz = dzmin; dz <= dzmax; dz++) {
      for (int dy = dymin; dy <= dymax; dy++) {
        for (int dx = dxmin; dx <= dxmax; dx++) {
          Patch3D patch = (Patch3D) world.getPatchAtWrap
              (startPatch.pxcor + dx, startPatch.pycor + dy, startPatch.pzcor + dz);

          if (patch != null) {
            if (sourceSet.kind() == AgentKindJ.Patch()) {
              // loop through our world copies
              outer:
              for (int worldOffsetX = -m; worldOffsetX <= m; worldOffsetX++) {
                for (int worldOffsetY = -n; worldOffsetY <= n; worldOffsetY++) {
                  for (int worldOffsetZ = -k; worldOffsetZ <= k; worldOffsetZ++) {
                    if ((sourceSet == world.patches() || sourceSet.contains(patch))
                        && isInCone(patch.pxcor + worldWidth * worldOffsetX,
                        patch.pycor + worldHeight * worldOffsetY,
                        patch.pzcor + worldDepth * worldOffsetZ,
                        startTurtle.xcor(), startTurtle.ycor(),
                        startTurtle.zcor(),
                        radius, half, startTurtle.heading(),
                        startTurtle.pitch())) {
                      result.add(patch);
                      break outer;
                    }
                  }
                }
              }
            } else {
              if (StrictMath.sqrt(dx * dx + dy * dy + dz * dz) <= radius + 1.415) {
                for (Turtle turtle : patch.turtlesHere()) {
                  // loop through our world copies
                  outer:
                  for (int worldOffsetX = -m; worldOffsetX <= m; worldOffsetX++) {
                    for (int worldOffsetY = -n; worldOffsetY <= n; worldOffsetY++) {
                      for (int worldOffsetZ = -k; worldOffsetZ <= k; worldOffsetZ++) {
                        // any turtle set with a non-null print name is either
                        // the set of all turtles, or a breed agentset - ST 2/19/04
                        if ((sourceSet == world.turtles() ||
                            (sourceSet.isBreedSet() && sourceSet == turtle.getBreed()) ||
                            (!sourceSet.isBreedSet() && sourceSet.contains(turtle))) &&
                            isInCone(turtle.xcor() + worldWidth * worldOffsetX,
                                turtle.ycor() + worldHeight * worldOffsetY,
                                ((Turtle3D) turtle).zcor() + worldDepth * worldOffsetZ,
                                startTurtle.xcor(), startTurtle.ycor(), startTurtle.zcor(),
                                radius, half, startTurtle.heading(),
                                startTurtle.pitch())) {
                          result.add(turtle);
                          break outer;
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    return result;
  }

  private boolean isInCone(double x, double y, double z,
                           double cx, double cy, double cz,
                           double r, double half, double h, double p) {
    Protractor3D protractor = world.protractor();

    if (x == cx && y == cy && z == cz) {
      return true;
    }
    if (protractor.distance(cx, cy, cz, x, y, z, false) > r) // false = don't wrap, since inCone()
    // handles wrapping its own way
    {
      return false;
    }

    Vect unitVect = Vect.toVectors(h, p, 0)[0];
    Vect targetVect = new Vect(x - cx, y - cy, z - cz);
    double angle = targetVect.angleTo(unitVect);
    double halfRadians = StrictMath.toRadians(half);

    return angle <= halfRadians || angle >= (2 * StrictMath.PI - halfRadians);
  }
}
