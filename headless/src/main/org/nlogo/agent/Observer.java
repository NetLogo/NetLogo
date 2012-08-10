// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

import org.nlogo.api.AgentException;
import org.nlogo.api.AgentKind;
import org.nlogo.api.AgentKindJ;
import org.nlogo.api.LogoException;
import org.nlogo.api.Perspective;
import org.nlogo.api.PerspectiveJ;
import org.nlogo.api.ValueConstraint;

public strictfp class Observer
    extends Agent
    implements org.nlogo.api.Observer {
  public Observer(World world) {
    super(world);
    resetPerspective();
  }

  public AgentKind kind() { return AgentKindJ.Observer(); }

  @Override
  Agent realloc(boolean forRecompile) {
    Object[] oldvars = variables;
    Object[] newvars = new Object[world.getVariablesArraySize(this)];
    ValueConstraint[] newcons = new ValueConstraint[world.getVariablesArraySize(this)];
    for (int i = 0; newvars.length != i; i++) {
      newvars[i] = World.ZERO;
      newcons[i] = null;
    }
    if (oldvars != null && forRecompile) {
      for (int i = 0; i < oldvars.length && i < world.oldProgram.globals().size(); i++) {
        String name = world.oldProgram.globals().apply(i);
        int newpos = world.observerOwnsIndexOf(name);
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

    return null;
  }

  @Override
  public Object getVariable(int vn) {
    return variables[vn];
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

  Perspective perspective = PerspectiveJ.OBSERVE();

  public Perspective perspective() {
    return perspective;
  }

  public void perspective(Perspective perspective) {
    this.perspective = perspective;
  }

  org.nlogo.api.Agent targetAgent = null;

  public org.nlogo.api.Agent targetAgent() {
    return targetAgent;
  }

  public void targetAgent(org.nlogo.api.Agent agent) {
    targetAgent = agent;
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
    if (perspective == PerspectiveJ.FOLLOW() || perspective == PerspectiveJ.RIDE()) {
      return _oxcor - ((world.minPxcor() - 0.5) + world.worldWidth() / 2.0);
    }
    return 0.0;
  }

  public double followOffsetY() {
    if (perspective == PerspectiveJ.FOLLOW() || perspective == PerspectiveJ.RIDE()) {
      return _oycor - ((world.minPycor() - 0.5) + world.worldHeight() / 2.0);
    }
    return 0.0;
  }

  double heading;

  public double heading() {
    return heading;
  }

  public void heading(double heading) {
    this.heading = ((heading % 360) + 360) % 360;
  }

  double pitch;

  public double pitch() {
    return pitch;
  }

  public void pitch(double pitch) {
    this.pitch = ((pitch % 360) + 360) % 360;
  }

  double roll;

  public double roll() {
    return roll;
  }

  public void roll(double roll) {
    this.roll = ((roll % 360) + 360) % 360;
  }

  public void setOrientation(double heading, double pitch, double roll) {
    this.heading = heading;
    this.pitch = pitch;
    this.roll = roll;
  }

  public double dx() {
    double value = StrictMath.cos(StrictMath.toRadians(pitch)) *
        StrictMath.sin(StrictMath.toRadians(heading));
    if (StrictMath.abs(value) < org.nlogo.api.Constants.Infinitesimal()) {
      value = 0;
    }
    return value;
  }

  public double dy() {
    double value = StrictMath.cos(StrictMath.toRadians(pitch)) *
        StrictMath.cos(StrictMath.toRadians(heading));
    if (StrictMath.abs(value) < org.nlogo.api.Constants.Infinitesimal()) {
      value = 0;
    }
    return value;
  }

  public double dz() {
    double value = StrictMath.sin(StrictMath.toRadians(pitch));
    if (StrictMath.abs(value) < org.nlogo.api.Constants.Infinitesimal()) {
      value = 0;
    }
    return value;
  }

  public void setPerspective(Perspective perspective, org.nlogo.api.Agent agent) {
    this.perspective = perspective;
    targetAgent = agent;
  }

  public void setPerspective(Perspective perspective) {
    this.perspective = perspective;
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
    setPerspective(PerspectiveJ.OBSERVE(), null);
    home();
  }

  public void home() {
    _oxcor = world.minPxcor() + ((world.maxPxcor() - world.minPxcor()) / 2.0);
    _oycor = world.minPycor() + ((world.maxPycor() - world.minPycor()) / 2.0);
    _ozcor = StrictMath.max(world.worldWidth(), world.worldHeight()) * 1.5;
    heading = 0;
    pitch = 90;
    roll = 0;
  }

  public boolean atHome2D() {
    return (perspective == PerspectiveJ.OBSERVE()) && (_oxcor == 0) && (_oycor == 0);
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

  public static final int BIT = AgentBit.apply(AgentKindJ.Observer());

  @Override
  public int agentBit() {
    return BIT;
  }

  public String shape() {
    return "";
  }

  public double size() {
    // how many observers can dance on the head of a pin?
    return 0;
  }

  public int alpha() {
    return 0;
  }

}
