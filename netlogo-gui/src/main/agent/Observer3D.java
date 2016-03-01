// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

import org.nlogo.api.AgentException;
import org.nlogo.api.AgentFollowingPerspective;
import org.nlogo.api.PerspectiveJ;
import org.nlogo.api.Vect;

public final strictfp class Observer3D
    extends Observer
    implements Agent3D {
  public Observer3D(World world) {
    super(world);
  }

  @Override
  public void home() {
    super.home();
    World3D w = (World3D) world;
    double zOff = w.minPzcor() + ((w.maxPzcor() - w.minPzcor()) / 2.0);
    ozcor(zOff + (StrictMath.max
        (world.worldWidth(),
         StrictMath.max(world.worldHeight(), w.worldDepth())) * 2));

    rotationPoint = new Vect(oxcor(), oycor(), zOff);
    right = new Vect(1, 0, 0);
    forward = new Vect(0, 0, 1);
  }

  @Override
  public boolean updatePosition() {
    boolean changed = false;

    if (perspective.kind() == PerspectiveJ.OBSERVE) {
      return false;
    } else if (perspective.kind() == PerspectiveJ.WATCH) {
      if (targetAgent() == null || targetAgent().id() == -1) {
        resetPerspective();
        return true;
      }

      face(targetAgent());
    } else // follow and ride are the same save initial conditions.
    {
      if (targetAgent() == null || targetAgent().id() == -1) // he's dead!
      {
        resetPerspective();
        return true;
      }

      Turtle3D turtle = (Turtle3D) targetAgent();
      oxyandzcor(turtle.xcor(), turtle.ycor(), turtle.zcor());
      // don't smooth for now, some turns don't have continuous
      // angles and the smoothing doesn't work properly ev 5/32/06
      heading(turtle.heading());
      pitch(turtle.pitch());
      roll(turtle.roll());
    }

    return changed;
  }

  public double distance(Agent agent) {
    double x, y, z;
    if (agent instanceof Turtle) {
      Turtle3D t = (Turtle3D) agent;
      x = t.xcor();
      y = t.ycor();
      z = t.zcor();
    } else {
      Patch3D p = (Patch3D) agent;
      x = p.pxcor;
      y = p.pycor;
      z = p.pzcor;
    }

    return distance(x, y, z);
  }

  @Override
  public double distance(org.nlogo.api.Turtle t) {
    return distance(t.xcor(), t.ycor(), ((org.nlogo.api.Turtle3D) t).zcor());
  }

  public double distance(double x, double y, double z) {
    return StrictMath.sqrt((x - oxcor()) * (x - oxcor())
        + (y - oycor()) * (y - oycor())
        + (z - ozcor()) * (z - ozcor()));
  }

  public double followOffsetZ() {
    if (perspective instanceof AgentFollowingPerspective) {
      World3D w = (World3D) world;
      return ozcor() - ((w.minPzcor() + w.maxPzcor()) / 2.0);
    }

    return 0.0;
  }

  public void face(double x, double y, double z) {
    try {
      heading(world.protractor().towards(this, x, y, false));
    } catch (AgentException ex) {
      heading(0.0);
    }
    try {
      pitch(-world.protractor().towardsPitch(this, x, y, z, false));
    } catch (AgentException ex) {
      pitch(0.0);
    }

    setRotationPoint(x, y, z);
    Vect[] v = Vect.toVectors(orientation.heading, orientation.pitch, orientation.roll);
    forward = v[0];
    right = v[1];
  }

  @Override
  public void moveto(Agent otherAgent) {
    if (otherAgent instanceof Turtle) {
      Turtle3D t = (Turtle3D) otherAgent;
      oxyandzcor(t.xcor(), t.ycor(), t.zcor());
    } else {
      Patch3D p = (Patch3D) otherAgent;
      oxyandzcor(p.pxcor, p.pycor, p.pzcor);
    }
    face(rotationPoint.x(), rotationPoint.y(), rotationPoint.z());
  }

  public Patch3D getPatchAtOffsets(double dx, double dy, double dz)
      throws AgentException {
    return ((World3D) world).getPatchAt(dx, dy, dz);
  }

  @Override
  public void setRotationPoint(org.nlogo.api.Agent agent) {
    if (agent instanceof Turtle) {
      Turtle3D t = (Turtle3D) agent;
      setRotationPoint(t.xcor(), t.ycor(), t.zcor());
    } else {
      Patch3D p = (Patch3D) agent;
      setRotationPoint(p.pxcor(), p.pycor(), p.pzcor());
    }
  }

  @Override
  public void orbitRight(double delta) {
    right = right.correct();
    forward = forward.correct();

    Vect cors = new Vect(oxcor(), oycor(), ozcor());
    Vect up = forward.cross(right);
    Vect xaxis = new Vect(1, 0, 0);
    Vect upxy = new Vect(up.x(), up.y(), 0);
    upxy = upxy.normalize();

    if (up.z() > 0) {
      delta = -delta;
    }

    cors = cors.subtract(rotationPoint);

    cors = cors.rotateZ(delta);
    right = right.rotateZ(delta);
    forward = forward.rotateZ(delta);

    cors = cors.add(rotationPoint);

    Vect rightxy = new Vect(right.x(), right.y(), 0);
    rightxy = rightxy.normalize();
    heading(StrictMath.toDegrees(rightxy.angleTo(xaxis)));

    oxyandzcor(cors.x(), cors.y(), cors.z());
  }

  private Vect forward;
  private Vect right;

  @Override
  public void orbitUp(double delta) {
    // translate the rotation point to the origin.
    Vect pos = new Vect(oxcor() - rotationPoint.x(),
        oycor() - rotationPoint.y(),
        ozcor() - rotationPoint.z());

    // use the right vector rather than the forward vector
    // to determine the "heading" so it is continuous.
    // might make craig less cranky to make this change
    // everywhere but not today.
    Vect rightxy =
        new Vect(right.x(), right.y(), 0)
            .correct().normalize();
    //measure from the x-axis because that's where
    // the right vector, rests and heading = 0
    Vect xaxis = new Vect(1, 0, 0);

    // convert to degrees since rotateX/Z expect degrees
    double angle = StrictMath.toDegrees(rightxy.angleTo(xaxis));

    // rotate around the z-axis so the rotation
    // can be made around the x-axis.
    pos = pos.rotateZ(angle).rotateX(-delta).rotateZ(-angle);

    pitch(orientation.pitch + delta);

    Vect[] v = Vect.toVectors(orientation.heading, orientation.pitch, orientation.roll);

    forward = v[0];
    right = v[1];

    oxyandzcor(pos.x() + orientation.rotationPoint.x(),
        pos.y() + orientation.rotationPoint.y(),
        pos.z() + orientation.rotationPoint.z());
  }

  @Override
  public void translate(double thetaX, double thetaY) {
    Vect[] v = Vect.toVectors(orientation.heading, orientation.pitch, orientation.roll);
    Vect ortho = v[1].cross(v[0]);

    oxcor(oxcor() - v[1].x() * thetaX * 0.1);
    oycor(oycor() - v[1].y() * thetaX * 0.1);
    ozcor(ozcor() + v[1].z() * thetaX * 0.1);

    rotationPoint = new Vect
        (rotationPoint.x() - v[1].x() * thetaX * 0.1,
         rotationPoint.y() - v[1].y() * thetaX * 0.1,
         rotationPoint.z() + v[1].z() * thetaX * 0.1);

    oxcor(oxcor() + ortho.x() * thetaY * 0.1);
    oycor(oycor() + ortho.y() * thetaY * 0.1);
    ozcor(ozcor() - ortho.z() * thetaY * 0.1);

    rotationPoint = new Vect
        (rotationPoint.x() + ortho.x() * thetaY * 0.1,
         rotationPoint.y() + ortho.y() * thetaY * 0.1,
         rotationPoint.z() - ortho.z() * thetaY * 0.1);
  }
}
