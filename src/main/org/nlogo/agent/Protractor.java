// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

import org.nlogo.api.AgentException;
import org.nlogo.api.Numbers;
import org.nlogo.api.I18N;

public strictfp class Protractor
    implements org.nlogo.api.Protractor {

  private final World world;

  Protractor(World world) {
    this.world = world;
  }

  /// distance/towards/at-distance

  // Distance to link is used to calculate the distance
  // between the mouse point and a link, for mouse interaction
  // in the view ev 5/7/08
  public double distanceToLink(Link link, double x, double y) {
    double x1 = link.x1();
    double y1 = link.y1();
    double x2 = link.x2();
    double y2 = link.y2();

    double xdiff = x2 - x1;
    double ydiff = y2 - y1;

    double[] p = closestPoint(x, y, x1, y1, xdiff, ydiff);

    if (inBounds(x1, y1, x2, y2, p[0], p[1])) {
      return distance(p[0], p[1], x, y, true);
    }

    double tmpx = x;
    double tmpy = y;

    if (x2 < world._minPxcor) {
      x -= world._worldWidth;
    } else if (x2 > world._maxPxcor) {
      x += world._worldWidth;
    }

    p = closestPoint(x, y, x1, y1, xdiff, ydiff);

    if (inBounds(x1, y1, x2, y2, p[0], p[1])) {
      return distance(p[0], p[1], x, y, true);
    }

    if (y2 < world._minPycor) {
      y -= world._worldHeight;
    } else if (y2 > world._maxPycor) {
      y += world._worldHeight;
    }

    p = closestPoint(x, y, x1, y1, xdiff, ydiff);

    if (inBounds(x1, y1, x2, y2, p[0], p[1])) {
      return distance(p[0], p[1], x, y, true);
    }

    x = tmpx;
    y = tmpy;

    p = closestPoint(x, y, x1, y1, xdiff, ydiff);

    if (inBounds(x1, y1, x2, y2, p[0], p[1])) {
      return distance(p[0], p[1], x, y, true);
    }

    // otherwise we know we are going to have to return one of the endpoints
    // there might be a better way to do this (find the line perpendicular
    // to the link through the midpoint and determine which side of the line we're
    // on?) but I don't know that's going to be any faster and it'll get messy with
    // wrapping, so just check the distance to both endpoints and return the smaller.
    return StrictMath.min(distance(x1, y1, x, y, true),
        distance(x2, y2, x, y, true));
  }

  private double[] closestPoint(double x, double y, double x1, double y1, double xdiff, double ydiff) {
    // all this math determines a point on the line defined by the endpoints of the
    // link nearest to the given point
    double u = ((x - x1) * xdiff + (y - y1) * ydiff) / (xdiff * xdiff + ydiff * ydiff);

    double xprime = x1 + u * xdiff;
    double yprime = y1 + u * ydiff;

    return new double[]{xprime, yprime};
  }

  public boolean inBounds(double x1, double y1, double x2, double y2, double x, double y) {
    // since this is a segment not a continuous line we have to check the bounds
    // we know it's a point on the line, so if it's in the bounding box then
    // we're good and just return that point. ev 10/12/06
    double top, bottom, right, left;
    if (y1 > y2) {
      top = y1;
      bottom = y2;
    } else {
      top = y2;
      bottom = y1;
    }
    if (x1 > x2) {
      right = x1;
      left = x2;
    } else {
      right = x2;
      left = x1;
    }

    if (x <= right && x >= left &&
        y <= top && y >= bottom) {
      return true;
    }

    return false;
  }

  // distance when no z coordinate is given should report
  // the distance in the xy-plane, not the distance
  // from the current location to ( x, y, 0 )
  public double distance(org.nlogo.api.Agent agent,
                         double x2, double y2,
                         boolean wrap) {
    double x1, y1;
    if (agent instanceof Turtle) {
      Turtle turtle = (Turtle) agent;
      x1 = turtle.xcor();
      y1 = turtle.ycor();
    } else if (agent instanceof Link) {
      return distanceToLink((Link) agent, x2, y2);
    } else {
      Patch patch = (Patch) agent;
      x1 = patch.pxcor;
      y1 = patch.pycor;
    }

    return distance(x1, y1, x2, y2, wrap);
  }

  public double distance(org.nlogo.api.Agent agent1, org.nlogo.api.Agent agent2,
                         boolean wrap) {
    double x1, y1;
    if (agent1 instanceof Turtle) {
      Turtle turtle = (Turtle) agent1;
      x1 = turtle.xcor();
      y1 = turtle.ycor();
    } else {
      Patch patch = (Patch) agent1;
      x1 = patch.pxcor;
      y1 = patch.pycor;
    }
    return distance(agent2, x1, y1, wrap);
  }

  public double distance(double x1, double y1,
                         double x2, double y2,
                         boolean wrap) {
    double dx = x2 - x1;
    double dy = y2 - y1;
    double distanceNoWrap = world.rootsTable.gridRoot(dx * dx + dy * dy);

    if (wrap) {
      double distanceWrap = world.topology().distanceWrap(dx, dy, x1, y1, x2, y2);

      if (distanceWrap < distanceNoWrap) {
        return distanceWrap;
      }
    }
    return distanceNoWrap;
  }

  // note this is very similar to Turtle.jump() - ST 9/3/03
  // heading must be in [0,360] range - ST 1/3/07
  public Patch getPatchAtHeadingAndDistance(Agent a, double heading, double distance)
      throws AgentException {
    if (a instanceof Turtle) {
      Turtle t = (Turtle) a;
      return getPatchAtHeadingAndDistance(t.xcor(), t.ycor(), heading, distance);
    } else {
      Patch p = (Patch) a;
      return getPatchAtHeadingAndDistance(p.pxcor, p.pycor, heading, distance);
    }
  }

  // heading must be in [0,360] range - ST 1/3/07
  public Patch getPatchAtHeadingAndDistance(double x, double y,
                                            double heading, double distance)
      throws AgentException {
    double cos;
    double sin;
    int integerHeading = (int) heading;
    if (heading == integerHeading) {
      cos = TrigTables.cos()[integerHeading];
      sin = TrigTables.sin()[integerHeading];
    } else {
      double headingRadians = StrictMath.toRadians(heading);
      cos = StrictMath.cos(headingRadians);
      sin = StrictMath.sin(headingRadians);
      if (StrictMath.abs(cos) < Numbers.Infinitesimal()) {
        cos = 0;
      }
      if (StrictMath.abs(sin) < Numbers.Infinitesimal()) {
        sin = 0;
      }
    }
    return world.getPatchAt(x + distance * sin,
        y + distance * cos);
  }

  public double towards(org.nlogo.api.Agent fromAgent, org.nlogo.api.Agent toAgent,
                        boolean wrap)
      throws AgentException {
    double x, y;
    if (fromAgent == toAgent) {
      throw new AgentException
          (I18N.errorsJ().get("org.nlogo.agent.Protractor.noHeadingFromAgentToSelf"));
    }
    if (toAgent instanceof Turtle) {
      Turtle turtle = (Turtle) toAgent;
      x = turtle.xcor();
      y = turtle.ycor();
    } else if (toAgent instanceof Link) {
      // this doesn't make sense in all cases
      // but for watch and follow it does.
      Link link = (Link) toAgent;
      x = link.midpointX();
      y = link.midpointY();
    } else {
      Patch patch = (Patch) toAgent;
      x = patch.pxcor;
      y = patch.pycor;
    }
    return towards(fromAgent, x, y, wrap);
  }

  /**
   * @return 0 <= result < 360
   */
  public double towards(org.nlogo.api.Agent fromAgent,
                        double toX, double toY,
                        boolean wrap)
      throws AgentException {
    double fromX, fromY;
    if (fromAgent instanceof Turtle) {
      Turtle turtle = (Turtle) fromAgent;
      fromX = turtle.xcor();
      fromY = turtle.ycor();
    } else if (fromAgent instanceof Observer) {
      Observer obs = (Observer) fromAgent;
      fromX = obs.oxcor();
      fromY = obs.oycor();
    } else if (fromAgent instanceof Patch) {
      Patch patch = (Patch) fromAgent;
      fromX = patch.pxcor;
      fromY = patch.pycor;
    } else {
      // if it's a link we can't find towards from
      throw new IllegalStateException("In towards: fromAgent must not be a link");
    }
    return towards(fromX, fromY, toX, toY, wrap);
  }

  /**
   * @return 0 <= result < 360
   */
  public double towards(double fromX, double fromY,
                        double toX, double toY,
                        boolean wrap)
      throws AgentException {
    if (fromX == toX && fromY == toY) {
      throw new AgentException
          (I18N.errorsJ().getN("org.nlogo.agent.Protractor.noHeadingFromPointToSelf", fromX, fromY));
    }
    double dx = toX - fromX;
    double dy = toY - fromY;
    if (wrap) {
      return world.topology().towardsWrap(dx, dy);
    }
    if (dx == 0) {
      return dy > 0 ? 0 : 180;
    }
    if (dy == 0) {
      return dx > 0 ? 90 : 270;
    }
    return
        (270 + StrictMath.toDegrees
            (StrictMath.PI + StrictMath.atan2(-dy, dx)))
            % 360;
  }

}
