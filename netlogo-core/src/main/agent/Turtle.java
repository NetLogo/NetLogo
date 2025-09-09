// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

import org.nlogo.core.AgentKind;
import org.nlogo.core.AgentKindJ;
import org.nlogo.core.Breed;
import org.nlogo.core.I18N;
import org.nlogo.core.LogoList;
import org.nlogo.core.Program;
import org.nlogo.api.AgentException;
import org.nlogo.api.AgentVariableNumbers;
import org.nlogo.api.AgentVariables;
import org.nlogo.api.Color;
import org.nlogo.api.Dump;
import org.nlogo.api.LogoException;
import org.nlogo.log.LogManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

// A note on wrapping: normally whether x and y coordinates wrap is a
// product of the topology.  But we also have the old "-nowrap" primitives
// that don't wrap regardless of what the topology is.  So that's why many
// methods like distance() and towards() take a boolean argument "wrap";
// it's true for the normal prims, false for the nowrap prims. - ST 5/24/06

public abstract class Turtle
    extends Agent
    implements org.nlogo.api.Turtle, AgentColors {

  public void setId(long id) {
    super.setId(id);
    _variables[VAR_WHO] = Double.valueOf(id);
  }

  public AgentKind kind() { return AgentKindJ.Turtle(); }

  public static final int VAR_WHO = AgentVariableNumbers.VAR_WHO;
  public static final int VAR_COLOR = AgentVariableNumbers.VAR_COLOR;
  public static final int VAR_HEADING = AgentVariableNumbers.VAR_HEADING;
  public static final int VAR_XCOR = AgentVariableNumbers.VAR_XCOR;
  public static final int VAR_YCOR = AgentVariableNumbers.VAR_YCOR;
  static final int VAR_SHAPE = AgentVariableNumbers.VAR_SHAPE;
  public static final int VAR_LABEL = AgentVariableNumbers.VAR_LABEL;
  private static final int VAR_LABELCOLOR = AgentVariableNumbers.VAR_LABELCOLOR;
  static final int VAR_BREED = AgentVariableNumbers.VAR_BREED;
  private static final int VAR_HIDDEN = AgentVariableNumbers.VAR_HIDDEN;
  private static final int VAR_SIZE = AgentVariableNumbers.VAR_SIZE;
  private static final int VAR_PENSIZE = AgentVariableNumbers.VAR_PENSIZE;
  private static final int VAR_PENMODE = AgentVariableNumbers.VAR_PENMODE;

  public int LAST_PREDEFINED_VAR = VAR_PENMODE;
  public int NUMBER_PREDEFINED_VARS = LAST_PREDEFINED_VAR + 1;

  public static final String PEN_UP = "up".intern();
  public static final String PEN_DOWN = "down".intern();
  public static final String PEN_ERASE = "erase".intern();


  void initvars(Double xcor, Double ycor, AgentSet breed) {
    _variables[VAR_COLOR] = Color.BoxedBlack();
    heading = 0;
    _variables[VAR_HEADING] = World.Zero();
    this.xcor = xcor.doubleValue();
    _variables[VAR_XCOR] = xcor;
    this.ycor = ycor.doubleValue();
    _variables[VAR_YCOR] = ycor;
    _variables[VAR_SHAPE] = _world.turtleBreedShapes().breedShape(breed);
    _variables[VAR_LABEL] = "";
    _variables[VAR_LABELCOLOR] = Color.BoxedWhite();
    _variables[VAR_BREED] = breed;
    _variables[VAR_HIDDEN] = Boolean.FALSE;
    _variables[VAR_SIZE] = World.One();
    _variables[VAR_PENSIZE] = World.One();
    _variables[VAR_PENMODE] = PEN_UP;
  }

  Turtle(World world, AgentSet breed, Double xcor, Double ycor, boolean getId) {
    super(world);
    Object[] variables = new Object[world.getVariablesArraySize(this, breed)];
    setVariables(variables);
    if (getId) {
      setId(world.newTurtleId());
      world.turtles().add(this);
    }
    initvars(xcor, ycor, breed);

    for (int i = LAST_PREDEFINED_VAR + 1; i < variables.length; i++) {
      variables[i] = World.Zero();
    }
    if (breed != world.turtles()) {
      ((TreeAgentSet) breed).add(this);
    }
    getPatchHere().addTurtle(this);
  }

  // creates a turtle that has id id, breed turtle, and is in the turtles agentset in
  // the idth slot in the agents array, if the slot was empty.  it is up to the caller to make sure
  // that the slot is open.  --mas 12/18/01
  Turtle(World world, long id) {
    this(world, world.turtles(), World.Zero(), World.Zero(), false);
    setId(id);
    world.turtles().add(this);
  }


  Turtle(World world) {
    super(world);
  }

  // The observant reader will notice that the abstract methods are
  // primarily those which depend on the world arity (2D/3D).
  public abstract Turtle hatch(TreeAgentSet breed);
  public abstract Patch getPatchAtOffsets(double dx, double dy) throws AgentException;
  public abstract Patch getPatchAtHeadingAndDistance(double delta, double distance) throws AgentException;
  public abstract void jump(double distance) throws AgentException;
  public abstract Patch getPatchHere();
  public abstract void moveToPatchCenter();
  public abstract void home();
  public abstract void face(Agent agent, boolean wrap);
  public abstract void turnRight(double delta);
  public abstract double dx();
  public abstract double dy();
  abstract void drawLine(double x0, double y0, double x1, double y1);
  abstract Turtle makeTurtle(World world);

  public Turtle hatch() {
    return hatch(getBreed());
  }

  public void die() {
    if (_id == -1) {
      return;
    }
    _world.linkManager().cleanupTurtle(this);
    Patch patch = getPatchHere();
    patch.removeTurtle(this);
    AgentSet breed = getBreed();
    if (breed != _world.turtles()) {
      ((TreeAgentSet) breed).remove(agentKey());
    }
    _world.removeLineThickness(this);
    _world.turtles().remove(agentKey());
    long oldId = this.id();
    setId(-1);
    Observer observer = _world.observer();
    if (this == observer.targetAgent()) {
      observer.updatePosition();
    }
    LogManager.turtleRemoved(oldId, breed.printName());
  }

  public double lineThickness() {
    if (_world != null) {
      return _world.lineThickness(this);
    }
    return 0.0;
  }

  Patch currentPatch = null;

  @Override
  public Agent realloc(Program oldProgram, Program newProgram) {
    return realloc(oldProgram, newProgram, null);
  }

  Agent realloc(Program oldProgram, Program program, AgentSet oldBreed) {
    boolean compiling = oldProgram != null;
    // first check if we recompiled and our breed disappeared!
    if (compiling && getBreed() != _world.turtles() &&
        _world.getBreed(getBreed().printName()) == null) {
      return this;
    }

    // stage 0: get ready
    Object[] oldvars = _variables;
    Object[] variables = new Object[_world.getVariablesArraySize(this, getBreed())];
    int turtlesOwnSize = _world.getVariablesArraySize((Turtle) null, _world.turtles());

    // stage 1: use arraycopy to copy over as many variables as possible
    // (if compiling it's just the predefined ones, if not compiling it's turtles-own too!)
    int numberToCopyDirectly = compiling ? NUMBER_PREDEFINED_VARS : turtlesOwnSize;
    System.arraycopy(oldvars, 0, variables, 0, numberToCopyDirectly);

    // stage 2: shift the turtles-own variables into their new positions
    // (unless we already did turtles-own during stage 1)
    if (compiling) {
      for (int i = NUMBER_PREDEFINED_VARS; i < turtlesOwnSize; i++) {
        String name = _world.turtlesOwnNameAt(i);
        int oldpos = oldProgram.turtlesOwn().indexOf(name);
        if (oldpos == -1) {
          variables[i] = World.Zero();
        } else {
          variables[i] = oldvars[oldpos];
          oldvars[oldpos] = null;
        }
      }
    }

    // stage 3: handle the BREED-own variables
    for (int i = turtlesOwnSize; i < variables.length; i++) {
      String name = _world.breedsOwnNameAt(getBreed(), i);
      int oldpos = compiling ? oldBreedsOwnIndexOf(oldProgram, getBreed(), name)
          : _world.breedsOwnIndexOf(oldBreed, name);
      if (oldpos == -1) {
        variables[i] = World.Zero();
      } else {
        variables[i] = oldvars[oldpos];
        oldvars[oldpos] = null;
      }
    }

    setVariables(variables);

    return null;
  }

  /**
   * used by Turtle.realloc()
   */
  private int oldBreedsOwnIndexOf(Program oldProgram, AgentSet breed, String name) {
    scala.Option<Breed> found = oldProgram.breeds().get(breed.printName());
    if (found.isEmpty()) {
      return -1;
    }
    int result = found.get().owns().indexOf(name);
    if (result == -1) {
      return -1;
    }
    return oldProgram.turtlesOwn().size() + result;
  }

  public String variableName(int vn) {
    if (vn < _world.program().turtlesOwn().size()) {
      return _world.turtlesOwnNameAt(vn);
    } else {
      return _world.breedsOwnNameAt(getBreed(), vn);
    }
  }

  @Override
  public void setVariable(int vn, Object value)
      throws AgentException {
    setTurtleVariable(vn, value);
  }

  @Override
  public Object getTurtleOrLinkVariable(String varName) {
    return getTurtleVariable(_world.program().turtlesOwn().indexOf(varName));
  }

  public Object getTurtleVariable(int vn) {
    return getVariable(vn);
  }

  @Override
  public Object getVariable(int vn) {
    Object[] variables = _variables;

    if (vn == VAR_WHO && variables[VAR_WHO] == null) {
      variables[VAR_WHO] = Double.valueOf(_id);
    } else if (vn == VAR_HEADING && variables[VAR_HEADING] == null) {
      variables[VAR_HEADING] = Double.valueOf(heading);
    } else if (vn == VAR_XCOR && variables[VAR_XCOR] == null) {
      variables[VAR_XCOR] = Double.valueOf(xcor);
    } else if (vn == VAR_YCOR && variables[VAR_YCOR] == null) {
      variables[VAR_YCOR] = Double.valueOf(ycor);
    }

    return variables[vn];
  }

  public double getTurtleVariableDouble(int vn) {
    switch (vn) {
      case VAR_WHO:
        return _id;
      case VAR_HEADING:
        return heading;
      case VAR_XCOR:
        return xcor;
      case VAR_YCOR:
        return ycor;
      case VAR_SIZE:
        return size();
      case VAR_PENSIZE:
        return penSize();
      default:
        throw new IllegalArgumentException
            (I18N.errorsJ().getN("org.nlogo.agent.Agent.notADoubleVariable", vn));
    }
  }

  @Override
  public Object getLinkBreedVariable(String name)
      throws AgentException {
    throw new AgentException
        (I18N.errorsJ().get("org.nlogo.agent.Turtle.cantAccessLinkWithoutSpecifyingLink"));
  }

  @Override
  public Object getBreedVariable(String name)
      throws AgentException {
    int vn = _world.breedsOwnIndexOf(getBreed(), name);
    if (name != null && vn == -1) {
      throw new AgentException(I18N.errorsJ().getN("org.nlogo.agent.Agent.breedDoesNotOwnVariable",
          getBreed().printName(), name));
    }
    return getTurtleVariable(vn);
  }

  public boolean ownsVariable(String name) {
    int vn = _world.breedsOwnIndexOf(getBreed(), name);
    if (vn != -1) {
      return true;
    }
    vn = _world.turtlesOwnIndexOf(name);
    if (vn != -1) {
      return true;
    }
    return false;
  }

  public Object getVariable(String name)
      throws AgentException {
    int vn = _world.breedsOwnIndexOf(getBreed(), name);
    if (vn == -1) {
      vn = _world.turtlesOwnIndexOf(name);
    }
    if (vn != -1) {
      return getTurtleVariable(vn);
    }
    throw new AgentException(I18N.errorsJ().getN("org.nlogo.agent.Agent.breedDoesNotOwnVariable", this.toString(), name));
  }

  @Override
  public Object getLinkVariable(int vn)
      throws AgentException {
    throw new AgentException
        (I18N.errorsJ().get("org.nlogo.agent.Turtle.cantAccessLinkWithoutSpecifyingLink"));
  }

  @Override
  public Object getPatchVariable(int vn) {
    return getPatchHere().getPatchVariable(vn);
  }

  @Override
  public void setTurtleOrLinkVariable(String varName, Object value)
      throws AgentException {
    setTurtleVariable(_world.program().turtlesOwn().indexOf(varName), value);
  }

  @Override
  public void setTurtleVariable(int vn, double value)
      throws AgentException {
    switch (vn) {
      case VAR_COLOR:
        colorDouble(Double.valueOf(value));
        break;
      case VAR_HEADING:
        heading(value);
        break;
      case VAR_XCOR:
        xcor(value);
        break;
      case VAR_YCOR:
        ycor(value);
        break;
      case VAR_SIZE:
        size(value);
        break;
      case VAR_PENSIZE:
        penSize(value);
        break;
      case VAR_WHO:
        throw new AgentException(I18N.errorsJ().get("org.nlogo.agent.Turtle.cantChangeWho"));
      default:
        throw new IllegalArgumentException(I18N.errorsJ().getN("org.nlogo.agent.Agent.notADoubleVariable", vn));
    }
  }

  @Override
  public void setTurtleVariable(int vn, Object value)
      throws AgentException {
    if (vn > LAST_PREDEFINED_VAR) {
      _variables[vn] = value;
    } else {
      switch (vn) {
        case VAR_COLOR:
          if (value instanceof Double) {
            colorDouble((Double) value);
          } else if (value instanceof LogoList) {
            color((LogoList) value, VAR_COLOR);
          } else {
            wrongTypeForVariable
                (AgentVariables.getImplicitTurtleVariables(false)[vn], Double.class, value);
          }
          break;
        case VAR_HEADING:
          if (value instanceof Double) {
            heading((Double) value);
          } else {
            wrongTypeForVariable
                (AgentVariables.getImplicitTurtleVariables(false)[vn], Double.class, value);
          }
          break;
        case VAR_XCOR:
          if (value instanceof Double) {
            xcor((Double) value);
          } else {
            wrongTypeForVariable
                (AgentVariables.getImplicitTurtleVariables(false)[vn], Double.class, value);
          }
          break;
        case VAR_YCOR:
          if (value instanceof Double) {
            ycor((Double) value);
          } else {
            wrongTypeForVariable
                (AgentVariables.getImplicitTurtleVariables(false)[vn], Double.class, value);
          }
          break;
        case VAR_SHAPE:
          if (value instanceof String) {
            String newShape = _world.checkTurtleShapeName((String) value);
            if (newShape == null) {
              throw new AgentException
                  (I18N.errorsJ().getN("org.nlogo.agent.Agent.shapeUndefined", value));
            }
            shape(newShape);
          } else {
            wrongTypeForVariable
                (AgentVariables.getImplicitTurtleVariables(false)[vn], String.class, value);
          }
          break;
        case VAR_LABEL:
          label(value);
          break;
        case VAR_LABELCOLOR:
          if (value instanceof Double) {
            labelColor(((Double) value).doubleValue());
          } else if (value instanceof LogoList) {
            labelColor((LogoList) value, VAR_LABELCOLOR);
          } else {
            wrongTypeForVariable
                (AgentVariables.getImplicitTurtleVariables(false)[vn], Double.class, value);
          }
          break;
        case VAR_BREED:
          if (value instanceof AgentSet) {
            AgentSet breed = (AgentSet) value;
            if (breed != _world.turtles() && !_world.isBreed(breed)) {
              throw new AgentException(I18N.errorsJ().get("org.nlogo.agent.Turtle.cantSetBreedToNonBreedAgentSet"));
            }
            setBreed(breed);
          } else {
            wrongTypeForVariable
                (AgentVariables.getImplicitTurtleVariables(false)[vn], AgentSet.class, value);
          }
          break;
        case VAR_HIDDEN:
          if (value instanceof Boolean) {
            hidden(((Boolean) value).booleanValue());
          } else {
            wrongTypeForVariable
                (AgentVariables.getImplicitTurtleVariables(false)[vn], Boolean.class, value);
          }
          break;
        case VAR_SIZE:
          if (value instanceof Double) {
            size(((Double) value).doubleValue());
          } else {
            wrongTypeForVariable
                (AgentVariables.getImplicitTurtleVariables(false)[vn],
                    Double.class, value);
          }
          break;
        case VAR_PENMODE:
          if (value instanceof String) {
            penMode((String) value);
          } else {
            wrongTypeForVariable(AgentVariables.getImplicitTurtleVariables(false)[vn],
                String.class, value);
          }
          break;

        case VAR_PENSIZE:
          if (value instanceof Double) {
            penSize(((Double) value).doubleValue());
          } else {
            wrongTypeForVariable(AgentVariables.getImplicitTurtleVariables(false)[vn],
                Double.class, value);
          }
          break;

        case VAR_WHO:
          throw new AgentException(I18N.errorsJ().get("org.nlogo.agent.Turtle.cantChangeWho"));
        default:
          throw new IllegalStateException(I18N.errorsJ().getN("org.nlogo.agent.Agent.cantSetUnknownVariable", vn));
      }
    }
    _world.notifyWatchers(this, vn, value);
  }

  @Override
  public void setBreedVariable(String name, Object value)
      throws AgentException {
    int vn = _world.breedsOwnIndexOf(getBreed(), name);
    if (name != null && vn == -1) {
      throw new AgentException(I18N.errorsJ().getN("org.nlogo.agent.Agent.breedDoesNotOwnVariable",
          getBreed().printName(), name));
    }
    setTurtleVariable(vn, value);
  }

  @Override
  public void setPatchVariable(int vn, Object value)
      throws AgentException {
    getPatchHere().setPatchVariable(vn, value);
  }

  @Override
  public void setPatchVariable(int vn, double value)
      throws AgentException {
    getPatchHere().setPatchVariable(vn, value);
  }

  @Override
  public void setLinkVariable(int vn, Object value)
      throws AgentException {
    throw new AgentException
        ("a turtle can't set a link variable without specifying which link");
  }

  @Override
  public void setLinkVariable(int vn, double value)
      throws AgentException {
    throw new AgentException
        ("a turtle can't set a link variable without specifying which link");
  }

  @Override
  public void setLinkBreedVariable(String name, Object value)
      throws AgentException {
    throw new AgentException
        ("a turtle can't set a link variable without specifying which link");
  }

  ///

  public Object color() {
    return _variables[VAR_COLOR];
  }

  public void colorDouble(Double boxedColor) {
    double c = boxedColor.doubleValue();
    if (c < 0 || c >= Color.MaxColor()) {
      c = Color.modulateDouble(c);
      boxedColor = Double.valueOf(c);
    }
    _variables[VAR_COLOR] = boxedColor;
  }

  public void colorDoubleUnchecked(Double boxedColor) {
    _variables[VAR_COLOR] = boxedColor;
  }

  public void color(LogoList rgb, int varIndex)
      throws AgentException {
    validRGBList(rgb, true);
    _variables[varIndex] = rgb;
    if(rgb.size() > 3) {
      _world.mayHavePartiallyTransparentObjects(true);
    }
  }

  double heading = 0;
  double cachedHeading = 0;
  double cachedSine = 0;
  double cachedCosine = 1;

  public double heading() {
    return heading;
  }

  public void heading(double heading) {
    double originalHeading = this.heading;
    headingHelper(heading);
    if (_world.tieManager().hasTies()) {
      _world.tieManager().turtleTurned(this, heading, originalHeading);
    }
  }

  // when we're calculating ties this gets called since we are recursing
  // and we need to pass the seenTurtles around ev 7/24/07
  public void heading(double heading, scala.collection.immutable.Set<Turtle> seenTurtles) {
    double originalHeading = this.heading;
    headingHelper(heading);
    if (_world.tieManager().hasTies()) {
      _world.tieManager().turtleTurned(this, heading, originalHeading, seenTurtles);
    }
  }

  private void headingHelper(double heading) {
    if (heading < 0 || heading >= 360) {
      heading = ((heading % 360) + 360) % 360;
    }
    this.heading = heading;
    _variables[VAR_HEADING] = null;
    Observer observer = _world.observer();
    if (this == observer.targetAgent()) {
      observer.updatePosition();
    }
  }

  public void heading(Double heading) {
    double originalHeading = this.heading;
    double h = heading.doubleValue();
    double wrapped;
    if (h < 0 || h >= 360) {
      wrapped = ((h % 360) + 360) % 360;
    } else {
      wrapped = h;
    }
    this.heading = wrapped;
    if (h == wrapped) {
      _variables[VAR_HEADING] = heading;
    } else {
      _variables[VAR_HEADING] = null;
    }
    Observer observer = _world.observer();
    if (this == observer.targetAgent()) {
      observer.updatePosition();
    }
    if (_world.tieManager().hasTies()) {
      _world.tieManager().turtleTurned(this, h, originalHeading);
    }
  }

  ///

  public void moveTo(Agent otherAgent)
      throws AgentException {
    double x, y;
    if (otherAgent instanceof Turtle) {
      Turtle t = (Turtle) otherAgent;
      x = t.xcor();
      y = t.ycor();
    } else if (otherAgent instanceof Patch) {
      Patch p = (Patch) otherAgent;
      x = p.pxcor;
      y = p.pycor;
    } else {
      throw new AgentException("you can't move-to a link");
    }

    xandycor(shortestPathX(x), shortestPathY(y));
  }

  public double shortestPathY(double y)
      throws AgentException {
    // if the pen is not even down we don't care
    // what the shortest path is so just return.
    if (penMode() != PEN_DOWN) {
      return y;
    }

    // wrap the coords (we do this first in case we're trying to move outside
    // the world we want to trigger the exception.)
    y = _world.wrapY(y);

    double yprime;

    // find the "unwrapped" coordinates
    // could we just be undoing what we just did? yes.
    // but do we really want to try and figure that out?
    if (y > ycor) {
      yprime = y - _world.worldHeight();
    } else {
      yprime = y + _world.worldHeight();
    }

    // technically we're not supposed to check these
    // directly but it seems like the clearest thing to
    // do here since we don't want to change the values
    // just make sure that we're obeying the topology if
    // we were to use one of these paths.
    if (!_world.wrappingAllowedInY()) {
      yprime = y;
    }

    if (StrictMath.abs(y - ycor) > StrictMath.abs(yprime - ycor)) {
      y = yprime;
    }

    return y;
  }

  public double shortestPathX(double x)
      throws AgentException {
    if (penMode() != PEN_DOWN) {
      return x;
    }

    x = _world.wrapX(x);

    double xprime;

    if (x > xcor) {
      xprime = x - _world.worldWidth();
    } else {
      xprime = x + _world.worldWidth();
    }

    if (!_world.wrappingAllowedInX()) {
      xprime = x;
    }

    if (StrictMath.abs(x - xcor) > StrictMath.abs(xprime - xcor)) {
      x = xprime;
    }

    return x;
  }

  double xcor;

  public double xcor() {
    return xcor;
  }

  public void xcor(double xcor)
      throws AgentException {
    Patch originalPatch = getPatchHere();

    double oldX = this.xcor;

    drawLine(this.xcor, ycor, shortestPathX(xcor), ycor);

    this.xcor = _world.wrapX(xcor);

    _variables[VAR_XCOR] = null;
    currentPatch = null;
    Patch targetPatch = getPatchHere();
    if (originalPatch != targetPatch) {
      originalPatch.removeTurtle(this);
      targetPatch.addTurtle(this);
    }
    Observer observer = _world.observer();
    if (this == observer.targetAgent()) {
      observer.updatePosition();
    }
    if (_world.tieManager().hasTies()) {
      _world.tieManager().turtleMoved
          (this, xcor, ycor, oldX, ycor);
    }
  }

  public void xcor(Double xcor)
      throws AgentException {
    Patch originalPatch = getPatchHere();
    double x = xcor.doubleValue();
    double oldX = this.xcor;

    double wrapped = _world.wrapX(x);

    drawLine(this.xcor, ycor, shortestPathX(x), ycor);

    this.xcor = wrapped;
    if (x == wrapped) {
      _variables[VAR_XCOR] = xcor;
    } else {
      _variables[VAR_XCOR] = null;
    }
    currentPatch = null;
    Patch targetPatch = getPatchHere();
    if (originalPatch != targetPatch) {
      originalPatch.removeTurtle(this);
      targetPatch.addTurtle(this);
    }
    Observer observer = _world.observer();
    if (this == observer.targetAgent()) {
      observer.updatePosition();
    }
    if (_world.tieManager().hasTies()) {
      _world.tieManager().turtleMoved
          (this, x, ycor, oldX, ycor);
    }
  }

  double ycor;

  public double ycor() {
    return ycor;
  }

  public void ycor(double ycor)
      throws AgentException {
    Patch originalPatch = getPatchHere();

    double oldY = this.ycor;

    drawLine(xcor, this.ycor, xcor, shortestPathY(ycor));

    this.ycor = _world.wrapY(ycor);

    _variables[VAR_YCOR] = null;
    currentPatch = null;
    Patch targetPatch = getPatchHere();
    if (originalPatch != targetPatch) {
      originalPatch.removeTurtle(this);
      targetPatch.addTurtle(this);
    }
    Observer observer = _world.observer();
    if (this == observer.targetAgent()) {
      observer.updatePosition();
    }
    if (_world.tieManager().hasTies()) {
      _world.tieManager().turtleMoved(this, xcor, ycor, xcor, oldY);
    }
  }

  public void ycor(Double ycor)
      throws AgentException {
    Patch originalPatch = getPatchHere();
    double y = ycor.doubleValue();
    double oldY = this.ycor;
    double wrapped = _world.wrapY(y);

    drawLine(xcor, this.ycor, xcor, shortestPathY(y));

    this.ycor = wrapped;
    if (y == wrapped) {
      _variables[VAR_YCOR] = ycor;
    } else {
      _variables[VAR_YCOR] = null;
    }
    currentPatch = null;
    Patch targetPatch = getPatchHere();
    if (originalPatch != targetPatch) {
      originalPatch.removeTurtle(this);
      targetPatch.addTurtle(this);
    }
    Observer observer = _world.observer();
    if (this == observer.targetAgent()) {
      observer.updatePosition();
    }
    if (_world.tieManager().hasTies()) {
      _world.tieManager().turtleMoved(this, xcor, y, xcor, oldY);
    }
  }

  public void xandycor(double xcor, double ycor)
      throws AgentException {
    xandycorHelper(xcor, ycor, false);
  }

  // when we're calculating ties this gets called since we are recursing
  // and we need to pass the seenTurtles around ev 7/24/07
  public void xandycor(double xcor, double ycor, scala.collection.immutable.Set<Turtle> seenTurtles, boolean isJump)
      throws AgentException {
    xandycorHelper(xcor, ycor, seenTurtles, isJump);
  }

  private final scala.collection.immutable.Set<Turtle> setOfSelf =
    Agent$.MODULE$.turtleSet(this);

  public void xandycorHelper(double xcor, double ycor, boolean isJump)
    throws AgentException {
    xandycorHelper(xcor, ycor, setOfSelf, isJump);
  }

  public void xandycorHelper(double xcor, double ycor, scala.collection.immutable.Set<Turtle> seenTurtles, boolean isJump)
      throws AgentException {
    double oldX = this.xcor;
    double oldY = this.ycor;

    Patch originalPatch = getPatchHere();

    double newX = _world.wrapX(xcor);
    double newY = _world.wrapY(ycor);

    if (! isJump) {
      drawLine(this.xcor, this.ycor, xcor, ycor);
    }

    this.xcor = newX;
    this.ycor = newY;

    _variables[VAR_XCOR] = null;
    _variables[VAR_YCOR] = null;
    currentPatch = null;
    Patch targetPatch = getPatchHere();
    if (originalPatch != targetPatch) {
      originalPatch.removeTurtle(this);
      targetPatch.addTurtle(this);
    }
    Observer observer = _world.observer();
    if (this == observer.targetAgent()) {
      observer.updatePosition();
    }
    if (_world.tieManager().hasTies()) {
      _world.tieManager().turtleMoved(this, xcor, ycor, oldX, oldY, seenTurtles);
    }
  }

  public void xandycor(Double xcor, Double ycor)
      throws AgentException {
    Patch originalPatch = getPatchHere();
    double x = xcor.doubleValue();
    double y = ycor.doubleValue();

    double oldX = this.xcor;
    double oldY = this.ycor;

    double wrappedX = _world.wrapX(x);
    double wrappedY = _world.wrapY(y);

    drawLine(this.xcor, this.ycor, x, y);

    this.xcor = wrappedX;
    this.ycor = wrappedY;
    _variables[VAR_XCOR] = (x == wrappedX) ? xcor : null;
    _variables[VAR_YCOR] = (y == wrappedY) ? ycor : null;
    currentPatch = null;
    Patch targetPatch = getPatchHere();
    if (originalPatch != targetPatch) {
      originalPatch.removeTurtle(this);
      targetPatch.addTurtle(this);
    }
    Observer observer = _world.observer();
    if (this == observer.targetAgent()) {
      observer.updatePosition();
    }
    if (_world.tieManager().hasTies()) {
      _world.tieManager().turtleMoved(this, x, y, oldX, oldY);
    }
  }

  public void face(double x, double y, boolean wrap) {
    try {
      heading(_world.protractor().towards(this, x, y, wrap));
    } catch (AgentException ex) {
      // AgentException here means we tried to calculate the heading from
      // an agent to itself, or to an agent at the exact same position.
      // Since face is nice, it just ignores the exception and doesn't change
      // the callers heading. - AZS 6/22/05
      org.nlogo.api.Exceptions.ignore(ex);
    }
  }

  public static double subtractHeadings(double h1, double h2) {
    if (h1 < 0 || h1 >= 360) {
      h1 = (h1 % 360 + 360) % 360;
    }
    if (h2 < 0 || h2 >= 360) {
      h2 = (h2 % 360 + 360) % 360;
    }
    double diff = h1 - h2;
    if (diff > -180 && diff <= 180) {
      return diff;
    } else if (diff > 0) {
      return diff - 360;
    } else {
      return diff + 360;
    }
  }

  public String shape() {
    return (String) _variables[VAR_SHAPE];
  }

  public void shape(String shape) {
    _variables[VAR_SHAPE] = shape;
  }

  public Object label() {
    return _variables[VAR_LABEL];
  }

  public boolean hasLabel() {
    return !(label() instanceof String &&
        ((String) label()).length() == 0);
  }

  public String labelString() {
    return Dump.logoObject(_variables[VAR_LABEL]);
  }

  public void label(Object label) {
    _variables[VAR_LABEL] = label;
  }

  public Object labelColor() {
    return _variables[VAR_LABELCOLOR];
  }

  public void labelColor(double labelColor) {
    _variables[VAR_LABELCOLOR] = Double.valueOf(Color.modulateDouble(labelColor));
  }

  public void labelColor(LogoList rgb, int valueIndex)
      throws AgentException {
    validRGBList(rgb, true);
    _variables[valueIndex] = rgb;
  }

  public TreeAgentSet getBreed() {
    return (TreeAgentSet) _variables[VAR_BREED];
  }

  // returns the index of the breed of this turtle, 0 means just a turtle
  // this is super kludge. is there a better way? -AZS 10/28/04
  @SuppressWarnings("unchecked")
  public int getBreedIndex() {
    AgentSet mybreed = getBreed();
    if (mybreed == _world.turtles()) {
      return 0;
    }
    int j = 0;
    for (Iterator<TreeAgentSet> iter = (Iterator<TreeAgentSet>)_world.breeds().values().iterator(); iter.hasNext(); j++) {
      if (mybreed == iter.next()) {
        return j;
      }
    }
    // we might get here if the program fails to compile ev 9/2/08
    return 0;
  }

  /**
   * This version of setBreed properly resets the global breed AgentSets
   * Caller should ensure that the turtle isn't a link (links aren't
   * allowed to change breed).
   */
  public void setBreed(AgentSet breed) {
    AgentSet oldBreed = null;
    if (_variables[VAR_BREED] instanceof AgentSet) {
      oldBreed = (AgentSet) _variables[VAR_BREED];
      if (breed == oldBreed) {
        return;
      }
      if (oldBreed != _world.turtles()) {
        ((TreeAgentSet) _variables[VAR_BREED]).remove(agentKey());
      }
    }
    if (breed != _world.turtles()) {
      ((TreeAgentSet) breed).add(this);
    }
    _variables[VAR_BREED] = breed;
    shape(_world.turtleBreedShapes().breedShape(breed));
    realloc(null, _world.program(), oldBreed);
  }

  public boolean hidden() {
    return ((Boolean) _variables[VAR_HIDDEN]).booleanValue();
  }

  public void hidden(boolean hidden) {
    _variables[VAR_HIDDEN] = hidden ? Boolean.TRUE : Boolean.FALSE;
  }

  public double size() {
    return ((Double) _variables[VAR_SIZE]).doubleValue();
  }

  public void size(double size) {
    _variables[VAR_SIZE] = Double.valueOf(size);
  }

  public double penSize() {
    return ((Double) _variables[VAR_PENSIZE]).doubleValue();
  }

  public void penSize(double penSize) {
    _variables[VAR_PENSIZE] = Double.valueOf(penSize);
  }

  public String penMode() {
    return (String) _variables[VAR_PENMODE];
  }

  public void penMode(String penMode) {
    _variables[VAR_PENMODE] = penMode.intern();
  }

  @Override
  public String toString() {
    return _world.getBreedSingular(getBreed()).toLowerCase() + " " + _id;
  }

  @Override
  public String classDisplayName() {
    return _world.getBreedSingular(getBreed()).toLowerCase();
  }

  public static final int BIT = 2;

  @Override
  public int agentBit() {
    return BIT;
  }

  public int alpha() {
    return org.nlogo.api.Color.getColor(color()).getAlpha();
  }

  private List<Link> links = null;

  public boolean isLinkedWith(Turtle dest, AgentSet linkSet) {
    if (links == null || links.isEmpty()) {
      return false;
    }

    boolean checkAllBreeds = linkSet == _world.links();
    boolean isBreedSet     = !linkSet.isBreedSet();

    for (Link link : links) {
      if (checkAllBreeds || (isBreedSet ? linkSet == link.getBreed() : linkSet.contains(link))) {
        Agent otherEnd = (link._end1 == this) ? link._end2 : link._end1;
        if (otherEnd == dest) {
          return true;
        }
      }
    }

    return false;
  }

  public boolean isLinkedTo(Turtle dest, AgentSet linkSet) {
    if (links == null || links.isEmpty()) {
      return false;
    }

    boolean checkAllBreeds = linkSet == _world.links();
    boolean isBreedSet     = !linkSet.isBreedSet();

    for (Link link : links) {
      if (checkAllBreeds || (isBreedSet ? linkSet == link.getBreed() : linkSet.contains(link))) {
        if (link.isDirectedLink()) {
          if (link._end1 == this && link._end2 == dest) {
            return true;
          }
        } else {
          Agent otherEnd = (link._end1 == this) ? link._end2 : link._end1;
          if (otherEnd == dest) {
            return true;
          }
        }
      }
    }

    return false;
  }

  public void addLink(Link link) {
    if (links == null) {
      links = new ArrayList<>(1);
    }
    links.add(link);
  }

  public boolean removeLink(Link link) {
    return links != null && links.remove(link);
  }

  public Link[] links() {
    return links == null ? new Link[0] : links.toArray(new Link[links.size()]);
  }

  public Link[] selectLinks(boolean out, boolean in, AgentSet linkSet) {
    if (links == null || links.isEmpty()) {
      return new Link[0];
    }

    boolean allBreeds = linkSet == _world.links();
    boolean notBreedSet = ! linkSet.isBreedSet();

    if (allBreeds && out && in) {
      return links();
    }

    Link[] result = new Link[links.size()];
    int writeTo = 0;
    for (Link link : links) {
      // check linkSet
      if (allBreeds || linkSet == link.getBreed() || (notBreedSet && linkSet.contains(link))) {
        // check directedness -- note that undirected links are *always* returned.
        if ((out && in) ||
            (out && link._end1 == this) ||
            (in  && link._end2 == this) ||
            !link.isDirectedLink()) {
          result[writeTo] = link;
          writeTo++;
        }
      }
    }
    if (writeTo == result.length) {
      return result;
    } else {
      return Arrays.copyOfRange(result, 0, writeTo);
    }
  }
}
