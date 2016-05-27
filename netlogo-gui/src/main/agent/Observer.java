// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

import org.nlogo.core.AgentKind;
import org.nlogo.core.AgentKindJ;
import org.nlogo.api.AgentException;
import org.nlogo.api.AgentFollowingPerspective;
import org.nlogo.api.LogoException;
import org.nlogo.api.ObserverOrientation;
import org.nlogo.api.Perspective;
import org.nlogo.api.PerspectiveJ;
import org.nlogo.api.ValueConstraint;
import org.nlogo.api.Vect;

public strictfp class Observer
    extends Agent
    implements org.nlogo.api.Observer {
  public Observer(World world) {
    super(world);
    resetPerspective();
  }

  private String[] varNames = new String[0];

  public AgentKind kind() { return AgentKindJ.Observer(); }

  @Override
  Agent realloc(boolean forRecompile) {
    Object[] oldvars = variables;
    String[] oldVarNames = varNames;
    Object[] newvars = new Object[world.getVariablesArraySize(this)];
    ValueConstraint[] newcons = new ValueConstraint[world.getVariablesArraySize(this)];
    String[] newVarNames = new String[world.getVariablesArraySize(this)];
    for (int i = 0; newvars.length != i; i++) {
      newvars[i] = World.ZERO;
      newcons[i] = null;
      newVarNames[i] = world.program().globals().apply(i);
    }

    if (oldvars != null && world.oldProgram() != null && forRecompile) {
      scala.collection.Seq<String> globals = world.program().globals();
      for (int i = 0; i < oldvars.length && i < oldVarNames.length; i++) {
        int newpos = globals.indexOf(oldVarNames[i]);
        if (newpos != -1) {
          newvars[newpos] = oldvars[i];
          // We do not populate the value constraints again.  When the widgets get compiled
          // they will be repopulated by the Workspace.  Otherwise, we end up with old
          // constraints around if you swap names of variables -- CLB
          //            newcons[ newpos ] = oldcons[ i ];
        }
      }
    }

    variables = newvars;
    variableConstraints = newcons;
    varNames = newVarNames;

    return null;
  }

  @Override
  public Object getVariable(int vn) {
    return variables[vn];
  }

  public String variableName(int vn) {
    return world.observerOwnsNameAt(vn);
  }

  public int variableIndex(String name) {
    for (int i = 0; i < varNames.length; i++) {
      if (varNames[i].equals(name))
        return i;
    }
    return -1;
  }

  @Override
  public Object getObserverVariable(int vn) {
    return variables[vn];
  }

  @Override
  public Object getTurtleVariable(int vn)
      throws AgentException {
    throw new AgentException("the observer can't access a turtle variable without specifying which turtle");
  }

  @Override
  public Object getTurtleOrLinkVariable(String varName)
      throws AgentException {
    throw new AgentException
        ("the observer can't access a turtle or link variable without specifying which agent");
  }

  @Override
  public Object getBreedVariable(String name)
      throws AgentException {
    throw new AgentException("the observer can't access a turtle variable without specifying which turtle");
  }

  @Override
  public Object getLinkVariable(int vn)
      throws AgentException {
    throw new AgentException
        ("the observer can't access a link variable without specifying which link");
  }

  @Override
  public Object getLinkBreedVariable(String name)
      throws AgentException {
    throw new AgentException
        ("the observer can't access a link variable without specifying which link");
  }

  @Override
  public Object getPatchVariable(int vn)
      throws AgentException {
    throw new AgentException("the observer can't access a patch variable without specifying which patch");
  }

  @Override
  public void setVariable(int vn, Object value)
      throws AgentException, LogoException {
    setObserverVariable(vn, value);
  }


  public void assertVariableConstraint(int vn, Object value)
      throws AgentException, LogoException {
    ValueConstraint con = variableConstraint(vn);
    if (con != null) {
      con.assertConstraint(value);
    }
  }

  @Override
  public void setObserverVariable(int vn, Object value)
      throws AgentException, LogoException {
    assertVariableConstraint(vn, value);
    variables[vn] = value;
    world.notifyWatchers(this, vn, value);
  }

  @Override
  public void setTurtleVariable(int vn, Object value)
      throws AgentException {
    throw new AgentException
        ("the observer can't set a turtle variable without specifying which turtle");
  }

  @Override
  public void setTurtleVariable(int vn, double value)
      throws AgentException {
    throw new AgentException("the observer can't set a turtle variable without specifying which turtle");
  }

  @Override
  public void setBreedVariable(String name, Object value)
      throws AgentException {
    throw new AgentException("the observer can't set a turtle variable without specifying which turtle");
  }

  @Override
  public void setPatchVariable(int vn, Object value)
      throws AgentException {
    throw new AgentException("the observer can't set a patch variable without specifying which turtle");
  }

  @Override
  public void setPatchVariable(int vn, double value)
      throws AgentException {
    throw new AgentException("the observer can't set a patch variable without specifying which turtle");
  }

  @Override
  public void setLinkVariable(int vn, Object value)
      throws AgentException {
    throw new AgentException
        ("the observer can't access a link variable without specifying which link");
  }

  @Override
  public void setLinkVariable(int vn, double value)
      throws AgentException {
    throw new AgentException
        ("the observer can't access a link variable without specifying which link");
  }

  @Override
  public void setLinkBreedVariable(String name, Object value)
      throws AgentException {
    throw new AgentException
        ("the observer can't access a link variable without specifying which link");
  }

  @Override
  public void setTurtleOrLinkVariable(String varName, Object value)
      throws AgentException {
    throw new AgentException
        ("the observer can't access a turtle or link variable without specifying which agent");
  }

  final HeadingSmoother headingSmoother = new HeadingSmoother();

  ///

  Perspective perspective = PerspectiveJ.create(PerspectiveJ.OBSERVE);

  public Perspective perspective() {
    return perspective;
  }

  public void perspective(Perspective perspective) {
    this.perspective = perspective;
  }

  public org.nlogo.api.Agent targetAgent() {
    if (perspective instanceof AgentFollowingPerspective) {
      return ((AgentFollowingPerspective) perspective).targetAgent();
    } else if (perspective instanceof Perspective.Watch) {
      return ((Perspective.Watch) perspective).targetAgent();
    } else {
      return null;
    }
  }

  ///

  private double _oxcor;
  public double oxcor() { return _oxcor; }
  public void oxcor(double oxcor) { _oxcor = oxcor; }

  private double _oycor;
  public double oycor() { return _oycor; }
  public void oycor(double oycor) { _oycor = oycor; }

  private double _ozcor;
  public double ozcor() { return _ozcor; }
  public void ozcor(double ozcor) { _ozcor = ozcor; }

  public void oxyandzcor(double oxcor, double oycor, double ozcor) {
    _oxcor = oxcor;
    _oycor = oycor;
    _ozcor = ozcor;
  }

  public double followOffsetX() {
    if (perspective instanceof AgentFollowingPerspective) {
      return _oxcor - ((world.minPxcor() - 0.5) + world.worldWidth() / 2.0);
    }
    return 0.0;
  }

  public double followOffsetY() {
    if (perspective instanceof AgentFollowingPerspective) {
      return _oycor - ((world.minPycor() - 0.5) + world.worldHeight() / 2.0);
    }
    return 0.0;
  }

  Orientation orientation = new Orientation(this);

  public scala.Option<ObserverOrientation> orientation() {
    return new scala.Some<ObserverOrientation>(orientation);
  }

  public void heading(double heading) {
    this.orientation.heading = ((heading % 360) + 360) % 360;
  }

  public void pitch(double pitch) {
    this.orientation.pitch = ((pitch % 360) + 360) % 360;
  }

  public void roll(double roll) {
    this.orientation.roll = ((roll % 360) + 360) % 360;
  }

  public void setRotationPoint(Vect v) {
    orientation.rotationPoint = v;
  }

  public void setRotationPoint(double x, double y, double z) {
    orientation.rotationPoint = new Vect(x, y, z);
  }

  public void setRotationPoint(org.nlogo.api.Agent agent) {
    if (agent instanceof org.nlogo.api.Turtle) {
      org.nlogo.api.Turtle t = (org.nlogo.api.Turtle) agent;
      setRotationPoint(t.xcor(), t.ycor(), 0);
    } else if (agent instanceof org.nlogo.api.Link) {
      org.nlogo.api.Link link = (org.nlogo.api.Link) agent;
      setRotationPoint(link.midpointX(), link.midpointY(), 0);
    } else {
      org.nlogo.api.Patch p = (org.nlogo.api.Patch) agent;
      setRotationPoint(p.pxcor(), p.pycor(), 0);
    }
  }

  public Vect rotationPoint() {
    return orientation.rotationPoint;
  }

  public int followDistance() {
    if (perspective instanceof AgentFollowingPerspective) {
      return ((AgentFollowingPerspective) perspective).followDistance();
    }
    return 5;
  }

  public void face(org.nlogo.api.Agent agent) {
    try {
      heading(world.protractor().towards(this, agent, false));
    } catch (AgentException ex) {
      heading(0.0);
    }
    try {
      pitch(-world.protractor().towardsPitch(this, agent, false));
    } catch (AgentException ex) {
      pitch(0.0);
    }

    setRotationPoint(agent);
  }

  public void face(double x, double y) {
    try {
      heading(world.protractor().towards(this, x, y, false));
    } catch (AgentException ex) {
      heading(0.0);
    }
    try {
      pitch(-world.protractor().towardsPitch(this, x, y, 0, false));
    } catch (AgentException ex) {
      pitch(0.0);
    }

    setRotationPoint(x, y, 0);
  }

  public void moveto(Agent otherAgent)
      throws AgentException {
    if (otherAgent instanceof Turtle) {
      Turtle t = (Turtle) otherAgent;
      oxyandzcor(t.xcor(), t.ycor(), 0);
    } else if (otherAgent instanceof Patch) {
      Patch p = (Patch) otherAgent;
      oxyandzcor(p.pxcor, p.pycor, 0);
    } else {
      throw new AgentException("you can't move-to a link");
    }
    face(orientation.rotationPoint.x(), orientation.rotationPoint.y());
  }

  public void setPerspective(Perspective perspective) {
    this.perspective = perspective;
    updatePosition();
  }

  public boolean updatePosition() {
    boolean changed = false;

    if (perspective.kind() == PerspectiveJ.OBSERVE) {
      return false;
    } else if (perspective.kind() == PerspectiveJ.WATCH && targetAgent() != null) {
      if (targetAgent().id() == -1) {
        resetPerspective();
        return true;
      }
      setRotationPoint(targetAgent());
      face(targetAgent());
    } else if (targetAgent() != null) {
      if (targetAgent().id() == -1) // he's dead!
      {
        resetPerspective();
        return true;
      }

      Turtle turtle = (Turtle) targetAgent();
      oxyandzcor(turtle.xcor(), turtle.ycor(), 0);
      double newHeading = headingSmoother.follow(targetAgent());
      if (perspective.kind() == PerspectiveJ.FOLLOW) {
        changed = orientation.heading != newHeading;
        heading(newHeading);
      } else {
        heading(turtle.heading());
      }

      pitch(0);
      roll(0);
    }

    return changed;
  }

  public double distance(org.nlogo.api.Agent agent) {
    double x, y;
    if (agent instanceof Turtle) {
      x = ((Turtle) agent).xcor();
      y = ((Turtle) agent).ycor();
    } else if (agent instanceof Link) {
      return world.protractor().distance(agent, _oxcor, _oycor, true);
    } else {
      x = ((Patch) agent).pxcor;
      y = ((Patch) agent).pycor;
    }

    return distance(x, y);

  }

  public double distance(org.nlogo.api.Turtle t) {
    return distance(t.xcor(), t.ycor());
  }

  double distance(double x, double y) {
    return StrictMath.sqrt((x - _oxcor) * (x - _oxcor)
        + (y - _oycor) * (y - _oycor)
        + _ozcor * _ozcor);
  }

  public void resetPerspective() {
    setPerspective(PerspectiveJ.create(PerspectiveJ.OBSERVE));
    home();
  }

  public void home() {
    _oxcor = world.minPxcor() + ((world.maxPxcor() - world.minPxcor()) / 2.0);
    _oycor = world.minPycor() + ((world.maxPycor() - world.minPycor()) / 2.0);
    _ozcor = StrictMath.max(world.worldWidth(), world.worldHeight()) * 1.5;
    orientation.heading = 0;
    orientation.pitch = 90;
    orientation.roll = 0;
    setRotationPoint(_oxcor, _oycor, 0);
  }

  public boolean atHome2D() {
    return (perspective.kind() == PerspectiveJ.OBSERVE) && (_oxcor == 0) && (_oycor == 0);
  }

  // This is a hack for now, there is prob. a better way of doing this - jrn 6/9/05
  public boolean atHome3D() {
    return (perspective.kind() == PerspectiveJ.OBSERVE) && (_oxcor == 0) && (_oycor == 0) &&
        (_ozcor == StrictMath.max(world.worldWidth(), world.worldHeight()) * 1.5) &&
        orientation.atHome3D();
  }

  @Override
  public Patch getPatchAtOffsets(double dx, double dy)
      throws AgentException {
    return world.getPatchAt(dx, dy);
  }

  @Override
  public String toString() {
    return "observer";
  }

  @Override
  public String classDisplayName() {
    return "observer";
  }

  @Override
  public Class<Observer> getAgentClass() {
    return Observer.class;
  }

  public static final int BIT = 1;

  @Override
  public int getAgentBit() {
    return BIT;
  }

  public String shape() {
    return "";
  }

  public double size() {
    // how many observers can dance on the head of a pin?
    return 0;
  }

  public void orbitRight(double delta) {
    orientation.orbitRight(delta);
  }

  public void orbitUp(double delta) {
    orientation.orbitUp(delta);
  }

  public void translate(double thetaX, double thetaY) {
    double headingR = StrictMath.toRadians(orientation.heading);
    double sinH = StrictMath.sin(headingR);
    double cosH = StrictMath.cos(headingR);

    _oxcor -= ((cosH * thetaX + sinH * thetaY) * 0.1);
    _oycor += ((sinH * thetaX - cosH * thetaY) * 0.1);

    orientation.rotationPoint = new Vect(orientation.rotationPoint.x() - ((cosH * thetaX + sinH * thetaY) * 0.1),
        orientation.rotationPoint.y() + ((sinH * thetaX - cosH * thetaY) * 0.1),
        orientation.rotationPoint.z());
  }

  public int alpha() {
    return 0;
  }

  class Orientation implements ObserverOrientation {
    double pitch;
    double heading;
    double roll;
    Vect rotationPoint;
    Observer observer;

    public Orientation(Observer observer) {
      this.observer = observer;
    }

    public double dist() {
      return StrictMath.sqrt((rotationPoint.x() - observer._oxcor) * (rotationPoint.x() - observer._oxcor)
          + (rotationPoint.y() - observer._oycor) * (rotationPoint.y() - observer._oycor)
          + ((rotationPoint.z() - observer._ozcor) * (rotationPoint.z() - observer._ozcor)));
    }

    public double heading() {
      return heading;
    }

    public double pitch() {
      return pitch;
    }

    public double roll() {
      return roll;
    }

    public double dx() {
      double value = StrictMath.cos(StrictMath.toRadians(orientation.pitch)) *
        StrictMath.sin(StrictMath.toRadians(orientation.heading));
      if (StrictMath.abs(value) < org.nlogo.api.Constants.Infinitesimal()) {
        value = 0;
      }
      return value;
    }

    public double dy() {
      double value = StrictMath.cos(StrictMath.toRadians(orientation.pitch)) *
        StrictMath.cos(StrictMath.toRadians(orientation.heading));
      if (StrictMath.abs(value) < org.nlogo.api.Constants.Infinitesimal()) {
        value = 0;
      }
      return value;
    }

    public double dz() {
      double value = StrictMath.sin(StrictMath.toRadians(orientation.pitch));
      if (StrictMath.abs(value) < org.nlogo.api.Constants.Infinitesimal()) {
        value = 0;
      }
      return value;
    }

    public boolean atHome3D() {
      return (heading == 0) && (pitch == 90) && (roll == 0) &&
        (rotationPoint.x() == 0 && rotationPoint.y() == 0 && rotationPoint.z() == 0);
    }

    public double normalizeDegrees(double d) {
      return ((d % 360) + 360) % 360;
    }

    public void orbitRight(double delta) {
      delta = -delta;

      double newHeading = heading + delta;
      double dxy = dist() * StrictMath.cos(StrictMath.toRadians(pitch));
      double x = -dxy * StrictMath.sin(StrictMath.toRadians(newHeading));
      double y = -dxy * StrictMath.cos(StrictMath.toRadians(newHeading));

      observer.oxyandzcor(x + rotationPoint.x(), y + rotationPoint.y(), _ozcor);
      heading = normalizeDegrees(newHeading);
    }

    public void orbitUp(double delta) {
      delta = -delta;

      double newPitch = pitch - delta;
      double z = dist() * StrictMath.sin(StrictMath.toRadians(newPitch));
      double dxy = dist() * StrictMath.cos(StrictMath.toRadians(newPitch));
      double x = -dxy * StrictMath.sin(StrictMath.toRadians(heading));
      double y = -dxy * StrictMath.cos(StrictMath.toRadians(heading));

      // don't let observer go under patch-plane or be upside-down
      if (z + orientation.rotationPoint.z() > 0 && newPitch < 90) {
        observer.oxyandzcor(x + rotationPoint.x(), y + rotationPoint.y(), z + rotationPoint.z());
        pitch = normalizeDegrees(newPitch);
      }
    }
  }
}
