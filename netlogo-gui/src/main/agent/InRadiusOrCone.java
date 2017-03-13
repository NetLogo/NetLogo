// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

import org.nlogo.api.AgentException;
import org.nlogo.core.AgentKindJ;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;

public strictfp class InRadiusOrCone {
  private final World world;

  InRadiusOrCone(World world) {
    this.world = world;
  }

  public List<Agent> inRadius(Agent agent, AgentSet sourceSet,
                              double radius, boolean wrap) {
    int worldWidth = world.worldWidth();
    int worldHeight = world.worldHeight();
    int maxPxcor = world.maxPxcor();
    int maxPycor = world.maxPycor();
    int minPxcor = world.minPxcor();
    int minPycor = world.minPycor();

    List<Agent> result = new ArrayList<Agent>();
    Patch startPatch;
    double startX, startY;

    if (agent instanceof Turtle) {
      Turtle startTurtle = (Turtle) agent;
      startPatch = startTurtle.getPatchHere();
      startX = startTurtle.xcor();
      startY = startTurtle.ycor();
    } else {
      startPatch = (Patch) agent;
      startX = startPatch.pxcor;
      startY = startPatch.pycor;
    }

    int dxmin = 0;
    int dxmax = 0;
    int dymin = 0;
    int dymax = 0;

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

    HashSet<Long> cachedIDs = null;
    if (! sourceSet.isBreedSet()) {
      cachedIDs = new HashSet<Long>(sourceSet.count());
      AgentIterator sourceTurtles = sourceSet.iterator();
      while (sourceTurtles.hasNext()) {
        Agent t = sourceTurtles.next();
        cachedIDs.add(new Long(t.id()));
      }
    } else {
      cachedIDs = new HashSet<Long>(0);
    }

    for (int dy = dymin; dy <= dymax; dy++) {
      for (int dx = dxmin; dx <= dxmax; dx++) {
        try {
          Patch patch = startPatch.getPatchAtOffsets(dx, dy);

          if (sourceSet.kind() == AgentKindJ.Patch()) {
            if (world.protractor().distance(patch.pxcor, patch.pycor, startX, startY, wrap) <= radius &&
                (sourceSet == world.patches() || cachedIDs.contains(new Long(patch.id())))) {
              result.add(patch);
            }
          } else if (sourceSet.kind() == AgentKindJ.Turtle()) {
            // Only check patches that might have turtles within the radius on them.
            // The 1.415 (square root of 2) adjustment is necessary because it is
            // possible for portions of a patch to be within the circle even though
            // the center of the patch is outside the circle.  Both turtles, the
            // turtle in the center and the turtle in the agentset, can be as much
            // as half the square root of 2 away from its patch center.  If they're
            // away from the patch centers in opposite directions, that makes a total
            // of square root of 2 additional distance we need to take into account.
            if (world.rootsTable.gridRoot(dx * dx + dy * dy) > radius + 1.415) {
              continue;
            }
            for (Turtle turtle : patch.turtlesHere()) {
              if (world.protractor().distance(turtle.xcor(), turtle.ycor(), startX, startY, wrap) <= radius
                      && (sourceSet == world.turtles()
                          || (sourceSet.isBreedSet() && sourceSet == turtle.getBreed())
                          || cachedIDs.contains(new Long(turtle.id())))) {
                result.add(turtle);
              }
            }
          }
        } catch (AgentException e) {
          org.nlogo.api.Exceptions.ignore(e);
        }
      }
    }
    return result;
  }

  public List<Agent> inCone(Turtle startTurtle, AgentSet sourceSet,
                            double radius, double angle, boolean wrap) {
    int worldWidth = world.worldWidth();
    int worldHeight = world.worldHeight();
    int maxPxcor = world.maxPxcor();
    int maxPycor = world.maxPycor();
    int minPxcor = world.minPxcor();
    int minPycor = world.minPycor();

    int m;
    int n;
    // If wrap is true and the radius is large enough, the cone
    // may wrap around the edges of the world.  We handle this by
    // enlarging the coordinate system in which we search beyond
    // the world edges and then filling the enlarged coordinate
    // system with "copies" of the world.  At least, you can
    // imagine it that way; we don't actually copy anything.  m
    // and n are the maximum number of times the cone might wrap
    // around the edge of the world in the X and Y directions, so
    // that's how many world copies we will need to make.  The
    // copies will range from -m to +m on the x axis and -n to +n
    // on the y axis.
    if (wrap) {
      m = world.wrappingAllowedInX() ? (int) StrictMath.ceil(radius / worldWidth) : 0;
      n = world.wrappingAllowedInY() ? (int) StrictMath.ceil(radius / worldHeight) : 0;
    } else {
      // in the nonwrapping case, we don't need any world copies besides
      // the original, so we have only one pair of offsets and both of
      // them are 0
      m = 0;
      n = 0;
    }

    List<Agent> result = new ArrayList<Agent>();
    Patch startPatch = startTurtle.getPatchHere();
    double half = angle / 2;
    // these four variables determine which patches we will check.
    // the patches we check always form a rectangle.  usually it
    // will be a square, but if the radius is large enough, then
    // it might be a rectangle.
    int dxmin = 0;
    int dymin = 0;
    int dxmax = 0;
    int dymax = 0;

    int r = (int) StrictMath.ceil(radius);

    // make sure to use half the world dimensions rather than just max-p(x/y)cor
    // since when the origin is off-center that may actually be 0 and thus
    // nothing gets searched ev 9/12/07
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

    HashSet<Long> cachedIDs = null;
    if (! sourceSet.isBreedSet()) {
      cachedIDs = new HashSet<Long>(sourceSet.count());
      AgentIterator sourceTurtles = sourceSet.iterator();
      while (sourceTurtles.hasNext()) {
        Agent t = sourceTurtles.next();
        cachedIDs.add(new Long(t.id()));
      }
    } else {
      cachedIDs = new HashSet<Long>(0);
    }

    // loop through the patches in the rectangle.  (it doesn't matter what
    // order we check them in.)
    for (int dy = dymin; dy <= dymax; dy++) {
      for (int dx = dxmin; dx <= dxmax; dx++) {
        // incone is optimized assuming a torus world making incone use the topology properly
        // will require a significant re-write. maybe it's  candidate for optimizations
        // for each topology.  ev 9/5/05
        Patch patch = world.getPatchAtWrap(startPatch.pxcor + dx, startPatch.pycor + dy);
        if (patch != null) {
          if (sourceSet.kind() == AgentKindJ.Patch()) {
            // loop through our world copies
            outer:
            for (int worldOffsetX = -m; worldOffsetX <= m; worldOffsetX++) {
              for (int worldOffsetY = -n; worldOffsetY <= n; worldOffsetY++) {
                if ((sourceSet == world.patches() || cachedIDs.contains(new Long(patch.id())))
                    && isInCone(patch.pxcor + worldWidth * worldOffsetX,
                    patch.pycor + worldHeight * worldOffsetY,
                    startTurtle.xcor(), startTurtle.ycor(),
                    radius, half, startTurtle.heading())) {
                  result.add(patch);
                  break outer;
                }
              }
            }
          } else {
            // Only check patches that might have turtles within the radius on them.
            // The 1.415 (square root of 2) adjustment is necessary because it is
            // possible for portions of a patch to be within the circle even though
            // the center of the patch is outside the circle.  Both turtles, the
            // turtle in the center and the turtle in the agentset, can be as much
            // as half the square root of 2 away from its patch center.  If they're
            // away from the patch centers in opposite directions, that makes a total
            // of square root of 2 additional distance we need to take into account.
            if (world.rootsTable.gridRoot(dx * dx + dy * dy) <= radius + 1.415) {
              for (Turtle turtle : patch.turtlesHere()) {
                // loop through our world copies
                outer:
                for (int worldOffsetX = -m; worldOffsetX <= m; worldOffsetX++) {
                  for (int worldOffsetY = -n; worldOffsetY <= n; worldOffsetY++) {
                    // any turtle set with a non-null print name is either
                    // the set of all turtles, or a breed agentset - ST 2/19/04
                    if ((sourceSet == world.turtles()
                            || (sourceSet.isBreedSet()  && sourceSet == turtle.getBreed())
                            || cachedIDs.contains(new Long(turtle.id())))
                            && isInCone(turtle.xcor() + worldWidth * worldOffsetX,
                                        turtle.ycor() + worldHeight * worldOffsetY,
                                        startTurtle.xcor(), startTurtle.ycor(),
                                        radius, half, startTurtle.heading())) {
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
    return result;
  }

  // helper method for inCone().
  // check if (x, y) is in the cone with center (cx, cy) , radius r, half-angle half, and central
  // line of the cone having heading h.
  private boolean isInCone(double x, double y,
                           double cx, double cy,
                           double r, double half, double h) {
    if (x == cx && y == cy) {
      return true;
    }
    if (world.protractor().distance(cx, cy, x, y, false) > r) // false = don't wrap, since inCone()
    // handles wrapping its own way
    {
      return false;
    }
    double theta;
    try {
      theta = world.protractor().towards(cx, cy, x, y, false);
    } catch (AgentException e) {
      // this should never happen because towards() only throws an AgentException
      // when the distance is 0, but we already ruled out that case above
      throw new IllegalStateException(e.toString());
    }
    double diff = StrictMath.abs(theta - h);
    // we have to be careful here because e.g. the difference between 5 and 355
    // is 10 not 350... hence the 360 thing
    return (diff <= half) || ((360 - diff) <= half);
  }
}
