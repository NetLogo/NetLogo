// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

import org.nlogo.core.I18N;
import org.nlogo.api.AgentException;

public strictfp class Turtle2D
  extends Turtle {

  public Turtle2D(World world, AgentSet breed, Double xcor, Double ycor) {
    super(world, breed, xcor, ycor, true);
  }

  private Turtle2D(World world, AgentSet breed, Double xcor, Double ycor, boolean getId) {
    super(world, breed, xcor, ycor, getId);
  }

  // creates a turtle that has id id, breed turtle, and is in the turtles agentset in
  // the idth slot in the agents array, if the slot was empty.  it is up to the caller to make sure
  // that the slot is open.  --mas 12/18/01
  Turtle2D(World world, long id) {
    this(world, world.turtles(), World.Zero(), World.Zero(), false);
  }

  Turtle2D(World world) {
    super(world);
  }

  public Turtle hatch() {
    return hatch(getBreed());
  }

  Turtle makeTurtle(World world) {
    return new Turtle2D(world);
  }

  @Override
  public Patch getPatchAtOffsets(double dx, double dy)
      throws AgentException {
    Patch target = world.getTopology().getPatchAt(xcor + dx, ycor + dy);
    if (target == null) {
      // Cannot get patch beyond limits of current world.
      throw new AgentException(I18N.errorsJ().get("org.nlogo.agent.Turtle.patchBeyondLimits"));
    }
    return target;
  }


  // note this is very similar to
  // World.getPatchAtDistanceAndHeading() - ST 9/3/03
  public void jump(double distance)
      throws AgentException {
    if (heading != cachedHeading) {
      cachedHeading = heading;
      int integerHeading = (int) heading;
      if (heading == integerHeading) {
        cachedCosine = TrigTables.cos()[integerHeading];
        cachedSine = TrigTables.sin()[integerHeading];
      } else {
        double headingRadians = StrictMath.toRadians(heading);
        cachedCosine = StrictMath.cos(headingRadians);
        cachedSine = StrictMath.sin(headingRadians);
      }
    }
    xandycor(xcor + (distance * cachedSine),
        ycor + (distance * cachedCosine));
  }

  public Patch getPatchHere() {
    if (currentPatch == null) {
      //turtles cannot leave the world, so xcor and ycor will always be valid
      //so assume we dont have to access the Topologies
      currentPatch = ((World2D) world).getPatchAtWrap(xcor, ycor);
    }
    return currentPatch;
  }

  void drawLine(double x0, double y0, double x1, double y1) {
    if (!penMode().equals(PEN_UP) && (x0 != x1 || y0 != y1)) {
      ((World2D) world).drawLine(x0, y0, x1, y1, variables[VAR_COLOR], penSize(), penMode());
    }
  }

  public void moveToPatchCenter() {
    Patch p = getPatchHere();
    double x = p.pxcor;
    double y = p.pycor;
    double oldX = this.xcor;
    double oldY = this.ycor;
    drawLine(oldX, oldY, x, y);
    if (x != oldX || y != oldY) {
      this.xcor = x;
      this.ycor = y;
      variables[VAR_XCOR] = p.variables[Patch.VAR_PXCOR];
      variables[VAR_YCOR] = p.variables[Patch.VAR_PYCOR];
      Observer observer = world.observer();
      if (this == observer.targetAgent()) {
        observer.updatePosition();
      }
      if (world.tieManager().hasTies()) {
        world.tieManager().turtleMoved(this, x, y, oldX, oldY);
      }
    }
  }

  public void home() {
    try {
      xandycor(World.Zero(), World.Zero());
    } catch (AgentException e) {
      // this will never happen since we require 0,0 be inside the world.
      throw new IllegalStateException(e);
    }
  }

  public void face(Agent agent, boolean wrap) {
    try {
      heading(world.protractor().towards(this, agent, wrap));
    } catch (AgentException ex) {
      // AgentException here means we tried to calculate the heading from
      // an agent to itself, or to an agent at the exact same position.
      // Since face is nice, it just ignores the exception and doesn't change
      // the callers heading. - AZS 6/22/05
      org.nlogo.api.Exceptions.ignore(ex);
    }
  }

  public double dx() {
    if (heading != cachedHeading) {
      cachedHeading = heading;
      int integerHeading = (int) heading;
      if (heading == integerHeading) {
        cachedCosine = TrigTables.cos()[integerHeading];
        cachedSine = TrigTables.sin()[integerHeading];
      } else {
        double headingRadians = StrictMath.toRadians(heading);
        cachedCosine = StrictMath.cos(headingRadians);
        cachedSine = StrictMath.sin(headingRadians);
      }
    }
    return cachedSine;
  }

  public double dy() {
    if (heading != cachedHeading) {
      cachedHeading = heading;
      int integerHeading = (int) heading;
      if (heading == integerHeading) {
        cachedCosine = TrigTables.cos()[integerHeading];
        cachedSine = TrigTables.sin()[integerHeading];
      } else {
        double headingRadians = StrictMath.toRadians(heading);
        cachedCosine = StrictMath.cos(headingRadians);
        cachedSine = StrictMath.sin(headingRadians);
      }
    }
    return cachedCosine;
  }

  public Patch getPatchAtHeadingAndDistance(double delta, double distance)
      throws AgentException {
    double h = heading + delta;
    if (h < 0 || h >= 360) {
      h = ((h % 360) + 360) % 360;
    }
    return world.protractor().getPatchAtHeadingAndDistance(this, h, distance);
  }

  public void turnRight(double delta) {
    heading(heading + delta);
  }
}
