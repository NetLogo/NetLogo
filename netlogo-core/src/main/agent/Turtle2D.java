// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

import org.nlogo.core.I18N;
import org.nlogo.api.AgentException;

public class Turtle2D
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
    setId(id);
    _world.turtles().add(this);
  }

  Turtle2D(World world) {
    super(world);
  }

  @Override
  public Turtle hatch(TreeAgentSet breed) {
    Turtle2D child = new Turtle2D((World2D) _world);
    child.heading = heading;
    child.xcor = xcor;
    child.ycor = ycor;
    child.setVariables(_variables.clone());
    child.setId(_world.newTurtleId());
    _world.turtles().add(child);
    if (breed != getBreed()) {
      child.setBreed(breed);
    }
    if (breed != _world.turtles()) {
      breed.add(child);
    }
    child.getPatchHere().addTurtle(child);
    return child;
  }

  Turtle makeTurtle(World world) {
    return new Turtle2D(world);
  }

  @Override
  public Patch getPatchAtOffsets(double dx, double dy)
      throws AgentException {
    Patch target = _world.getPatchAt(xcor + dx, ycor + dy);
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

    if (penMode() != PEN_UP) {
      try {
        Patch ignore = getPatchAtHeadingAndDistance(0, distance);
        drawLine(xcor, ycor, distance);
      } catch (AgentException ex) {
        // We end up here if `getPatchAtHeadingAndDistance` can't reach the patch that
        // far away (due to wrapping difficulties) --JAB (11/2/16)
        org.nlogo.api.Exceptions.ignore(ex);
      }
    }

    xandycorHelper(xcor + (distance * cachedSine),
        ycor + (distance * cachedCosine),
        true);
  }

  public Patch getPatchHere() {
    if (currentPatch == null) {
      //turtles cannot leave the world, so xcor and ycor will always be valid
      //so assume we dont have to access the Topologies
      currentPatch = ((World2D) _world).getPatchAtWrap(xcor, ycor);
    }
    return currentPatch;
  }

  void drawLine(double xcor, double ycor, double distance) {
    if (penMode() != PEN_UP) {
      Object color = variables()[VAR_COLOR];
      double size = penSize();
      String mode = penMode();
      double minPxcor = world().minPxcor() - 0.5;
      double maxPxcor = world().maxPxcor() + 0.5;
      double minPycor = world().minPycor() - 0.5;
      double maxPycor = world().maxPycor() + 0.5;
      Trail[] lines   = PenLineMaker.jumpLine(xcor, ycor, heading, distance, minPxcor, maxPxcor, minPycor, maxPycor);
      for (Trail line : lines) {
        ((World2D) _world).drawLine(line.x1(), line.y1(), line.x2(), line.y2(), color, size, mode);
      }
    }
  }

  void drawLine(double x0, double y0, double x1, double y1) {
    if (penMode() != PEN_UP && (x0 != x1 || y0 != y1)) {
      Object color    = variables()[VAR_COLOR];
      double size     = penSize();
      String mode     = penMode();
      double minPxcor = world().minPxcor() - 0.5;
      double maxPxcor = world().maxPxcor() + 0.5;
      double minPycor = world().minPycor() - 0.5;
      double maxPycor = world().maxPycor() + 0.5;

      Trail[] lines;
      if ((minPxcor < x0 && x0 < maxPxcor) && (minPxcor < x1 && x1 < maxPxcor) && (minPycor < y0 && y0 < maxPycor) && (minPycor < y1 && y1 < maxPycor)) {
        lines = PenLineMaker.translate(x0, y0, x1, y1);
      } else {
        double jumpDist = StrictMath.sqrt(StrictMath.pow(x0 - x1, 2) + StrictMath.pow(y0 - y1, 2));
        double dx       = x1 - x0;
        double dy       = y1 - y0;
        double jumpHead = world().topology().towardsWrap(dx, dy);
        lines           = PenLineMaker.jumpLine(x0, y0, jumpHead, jumpDist, minPxcor, maxPxcor, minPycor, maxPycor);
      }
      for (Trail line : lines) {
        ((World2D) _world).drawLine(line.x1(), line.y1(), line.x2(), line.y2(), color, size, mode);
      }
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
      _variables[VAR_XCOR] = p._variables[Patch.VAR_PXCOR];
      _variables[VAR_YCOR] = p._variables[Patch.VAR_PYCOR];
      Observer observer = _world.observer();
      if (this == observer.targetAgent()) {
        observer.updatePosition();
      }
      if (_world.tieManager().hasTies()) {
        _world.tieManager().turtleMoved(this, x, y, oldX, oldY);
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
      heading(_world.protractor().towards(this, agent, wrap));
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
    return _world.protractor().getPatchAtHeadingAndDistance(this, h, distance);
  }

  public void turnRight(double delta) {
    heading(heading + delta);
  }
}
