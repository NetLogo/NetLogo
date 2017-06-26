// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

import org.nlogo.api.AgentException;

public strictfp class Protractor3D
    extends Protractor
    implements org.nlogo.api.Protractor3D {

  private final World3D world;

  Protractor3D(World3D world) {
    super(world);
    this.world = world;
  }

  @Override
  public double distance(org.nlogo.api.Agent agent1, org.nlogo.api.Agent agent2,
                         boolean wrap) {
    double x1, y1, z1;
    if (agent1 instanceof Turtle) {
      Turtle3D turtle = (Turtle3D) agent1;
      x1 = turtle.xcor();
      y1 = turtle.ycor();
      z1 = turtle.zcor();
    } else if (agent1 instanceof Link) {
      throw new IllegalStateException("you can't find distance to links");
    } else {
      Patch3D patch = (Patch3D) agent1;
      x1 = patch.pxcor;
      y1 = patch.pycor;
      z1 = patch.pzcor;
    }
    return distance(agent2, x1, y1, z1, wrap);
  }

  public double distance(org.nlogo.api.Agent agent,
                         double x1, double y1, double z1,
                         boolean wrap) {
    double x2, y2, z2;
    if (agent instanceof Turtle) {
      Turtle3D turtle = (Turtle3D) agent;
      x2 = turtle.xcor();
      y2 = turtle.ycor();
      z2 = turtle.zcor();
    } else {
      Patch3D patch = (Patch3D) agent;
      x2 = patch.pxcor;
      y2 = patch.pycor;
      z2 = patch.pzcor;
    }

    return distance(x1, y1, z1, x2, y2, z2, wrap);
  }

  public double distance(double x1, double y1, double z1,
                         double x2, double y2, double z2,
                         boolean wrap) {
    double dx = -StrictMath.abs(x2 - x1);
    double dy = -StrictMath.abs(y2 - y1);
    double dz = -StrictMath.abs(z2 - z1);
    double distanceNoWrap = StrictMath.sqrt(dx * dx + dy * dy + dz * dz);

    if (wrap) {
      double distanceWrap = ((Topology3D) world.topology()).distanceWrap
          (dx, dy, dz, x1, y1, z1, x2, y2, z2);

      if (distanceWrap < distanceNoWrap) {
        return distanceWrap;
      }
    }
    return distanceNoWrap;
  }

  @Override
  public Patch getPatchAtHeadingAndDistance(Agent a, double heading, double distance)
      throws AgentException {
    if (a instanceof Turtle) {
      Turtle3D t = (Turtle3D) a;
      return getPatchAtHeadingPitchAndDistance(t.xcor(), t.ycor(), t.zcor(),
          heading, t.pitch(), distance);
    } else {
      Patch3D p = (Patch3D) a;
      return getPatchAtHeadingPitchAndDistance(p.pxcor, p.pycor, p.pzcor,
          heading, 0, distance);
    }
  }

  public Patch getPatchAtHeadingPitchAndDistance(double x, double y, double z,
                                                 double heading, double pitch,
                                                 double distance)
      throws AgentException {
    double pitchRadians = StrictMath.toRadians(pitch);
    double sin = StrictMath.sin(pitchRadians);
    double distProj = distance * StrictMath.cos(pitchRadians);
    if (StrictMath.abs(sin) < org.nlogo.api.Numbers.Infinitesimal()) {
      sin = 0;
    }
    if (StrictMath.abs(distProj) < org.nlogo.api.Numbers.Infinitesimal()) {
      distProj = 0;
    }

    double headingRadians = StrictMath.toRadians(heading);
    double cosProj = StrictMath.cos(headingRadians);
    double sinProj = StrictMath.sin(headingRadians);

    if (StrictMath.abs(cosProj) < org.nlogo.api.Numbers.Infinitesimal()) {
      cosProj = 0;
    }
    if (StrictMath.abs(sinProj) < org.nlogo.api.Numbers.Infinitesimal()) {
      sinProj = 0;
    }

    return world.getPatchAt(x + (distProj * sinProj),
        y + (distProj * cosProj),
        z + (distance * sin));
  }

  @Override
  public double towardsPitch(org.nlogo.api.Agent fromAgent, org.nlogo.api.Agent toAgent,
                             boolean wrap)
      throws AgentException {
    double x, y, z;
    if (fromAgent == toAgent) {
      throw new AgentException
          ("no pitch is defined from an agent to itself");
    }
    if (toAgent instanceof Turtle) {
      Turtle3D turtle = (Turtle3D) toAgent;
      x = turtle.xcor();
      y = turtle.ycor();
      z = turtle.zcor();
    } else {
      Patch3D patch = (Patch3D) toAgent;
      x = patch.pxcor;
      y = patch.pycor;
      z = patch.pzcor;
    }
    return towardsPitch(fromAgent, x, y, z, wrap);
  }


  @Override
  public double towardsPitch(org.nlogo.api.Agent fromAgent,
                             double toX, double toY, double toZ,
                             boolean wrap)
      throws AgentException {
    double fromX, fromY, fromZ;
    if (fromAgent instanceof Turtle) {
      Turtle3D turtle = (Turtle3D) fromAgent;
      fromX = turtle.xcor();
      fromY = turtle.ycor();
      fromZ = turtle.zcor();
    } else if (fromAgent instanceof Observer) {
      Observer obs = (Observer) fromAgent;
      fromX = obs.oxcor();
      fromY = obs.oycor();
      fromZ = obs.ozcor();
    } else {
      Patch3D patch = (Patch3D) fromAgent;
      fromX = patch.pxcor;
      fromY = patch.pycor;
      fromZ = patch.pzcor;
    }
    return towardsPitch(fromX, fromY, fromZ, toX, toY, toZ, wrap);
  }

  @Override
  public double towardsPitch(double fromX, double fromY, double fromZ,
                             double toX, double toY, double toZ,
                             boolean wrap)
      throws AgentException {
    if (fromX == toX && fromY == toY && fromZ == toZ) {
      throw new AgentException
          ("no pitch is defined from a point (" +
              fromX + "," + fromY + "," + fromZ + ") to that same point");
    }
    double dx = toX - fromX;
    double dy = toY - fromY;
    double dz = toZ - fromZ;
    if (wrap) {
      return ((Topology3D) world.topology()).towardsPitchWrap(dx, dy, dz);
    }

    return ((360 + StrictMath.toDegrees
        (StrictMath.atan(dz / StrictMath.sqrt(dx * dx + dy * dy)))) % 360);
  }


  public double[] towardsVector(double fromX, double fromY, double fromZ,
                                double toX, double toY, double toZ,
                                boolean wrap)
      throws AgentException {
    if (fromX == toX && fromY == toY && fromZ == toZ) {
      throw new AgentException
          ("no pitch is defined from a point (" +
              fromX + "," + fromY + "," + fromZ + ") to that same point");
    }
    double dx = toX - fromX;
    double dy = toY - fromY;
    double dz = toZ - fromZ;
    if (wrap) {
      dx = Topology.wrap(dx, world.minPxcor() - 0.5, world.maxPxcor() + 0.5);
      dy = Topology.wrap(dy, world.minPycor() - 0.5, world.maxPycor() + 0.5);
      dz = Topology.wrap(dz, world.minPzcor() - 0.5, world.maxPzcor() + 0.5);
    }

    return new double[]{dx, dy, dz};
  }
}
