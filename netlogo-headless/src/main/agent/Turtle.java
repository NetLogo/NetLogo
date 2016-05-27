// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

import org.nlogo.core.AgentKind;
import org.nlogo.core.AgentKindJ;
import org.nlogo.api.AgentException;
import org.nlogo.api.AgentVariableNumbers;
import org.nlogo.core.AgentVariables;
import org.nlogo.api.Color;
import org.nlogo.api.Dump;
import org.nlogo.core.I18N;
import org.nlogo.core.LogoList;

import java.util.Set;

public strictfp class Turtle
    extends Agent
    implements org.nlogo.api.Turtle {

  @Override
  public void _id_$eq(long id) {
    super._id_$eq(id);
    variables()[VAR_WHO] = Double.valueOf(id);
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

  public static final String PEN_UP = "up";
  public static final String PEN_DOWN = "down";
  public static final String PEN_ERASE = "erase";


  void initvars(Double xcor, Double ycor, AgentSet breed) {
    variables()[VAR_COLOR] = Color.BoxedBlack();
    heading = 0;
    variables()[VAR_HEADING] = World.ZERO;
    this.xcor = xcor.doubleValue();
    variables()[VAR_XCOR] = xcor;
    this.ycor = ycor.doubleValue();
    variables()[VAR_YCOR] = ycor;
    variables()[VAR_SHAPE] = world().turtleBreedShapes.breedShape(breed);
    variables()[VAR_LABEL] = "";
    variables()[VAR_LABELCOLOR] = Color.BoxedWhite();
    variables()[VAR_BREED] = breed;
    variables()[VAR_HIDDEN] = Boolean.FALSE;
    variables()[VAR_SIZE] = World.ONE;
    variables()[VAR_PENSIZE] = World.ONE;
    variables()[VAR_PENMODE] = PEN_UP;
  }

  public Turtle(World world, AgentSet breed, Double xcor, Double ycor) {
    this(world, breed, xcor, ycor, true);
  }

  private Turtle(World world, AgentSet breed, Double xcor, Double ycor, boolean getId) {
    super(world);
    _variables_$eq(new Object[world().getVariablesArraySize(this, breed)]);
    if (getId) {
      _id_$eq(world().newTurtleId());
      world()._turtles.add(this);
    }
    initvars(xcor, ycor, breed);

    for (int i = LAST_PREDEFINED_VAR + 1; i < variables().length; i++) {
      variables()[i] = World.ZERO;
    }
    if (breed != world().turtles()) {
      ((TreeAgentSet) breed).add(this);
    }
    getPatchHere().addTurtle(this);
  }

  // creates a turtle that has id id, breed turtle, and is in the turtles agentset in
  // the idth slot in the agents array, if the slot was empty.  it is up to the caller to make sure
  // that the slot is open.  --mas 12/18/01
  Turtle(World world, long id) {
    this(world, world.turtles(), World.ZERO, World.ZERO, false);
    _id_$eq(id);
    world._turtles.add(this);
  }


  Turtle(World world) {
    super(world);
  }

  public Turtle hatch(AgentSet breed) {
    Turtle child = new Turtle(world());
    child.heading = heading;
    child.xcor = xcor;
    child.ycor = ycor;
    child._variables_$eq(variables().clone());
    child._id_$eq(world().newTurtleId());
    world()._turtles.add(child);
    if (breed != getBreed()) {
      child.setBreed(breed);
    }
    else if (breed != world().turtles()) {
      ((TreeAgentSet) getBreed()).add(child);
    }
    child.getPatchHere().addTurtle(child);
    return child;
  }

  public void die() {
    if (id() == -1) {
      return;
    }
    world().linkManager().cleanupTurtle(this);
    Patch patch = getPatchHere();
    patch.removeTurtle(this);
    TreeAgentSet breed = (TreeAgentSet) getBreed();
    if (breed != world().turtles()) {
      breed.remove(agentKey());
    }
    world().removeLineThickness(this);
    world()._turtles.remove(agentKey());
    _id_$eq(-1);
  }

  public double lineThickness() {
    if (world() != null) {
      return world().lineThickness(this);
    }
    return 0.0;
  }

  Patch currentPatch = null;

  @Override
  public Patch getPatchAtOffsets(double dx, double dy)
      throws AgentException {
    Patch target = world().getPatchAt(xcor + dx, ycor + dy);
    if (target == null) {
      // Cannot get patch beyond limits of current world.
      throw new AgentException(I18N.errorsJ().get("org.nlogo.agent.Turtle.patchBeyondLimits"));
    }
    return target;
  }

  @Override
  public void realloc(boolean compiling) {
    realloc(compiling, null);
  }

  void realloc(boolean compiling, AgentSet oldBreed) {

    // stage 0: get ready
    Object[] oldvars = variables();
    _variables_$eq(new Object[world().getVariablesArraySize(this, getBreed())]);
    int turtlesOwnSize = world().program().turtlesOwn().size();

    // stage 1: use arraycopy to copy over as many variables as possible
    // (if compiling it's just the predefined ones, if not compiling it's turtles-own too!)
    int numberToCopyDirectly = compiling ? NUMBER_PREDEFINED_VARS : turtlesOwnSize;
    System.arraycopy(oldvars, 0, variables(), 0, numberToCopyDirectly);

    // stage 2: shift the turtles-own variables into their new positions
    // (unless we already did turtles-own during stage 1)
    if (compiling) {
      for (int i = NUMBER_PREDEFINED_VARS; i < turtlesOwnSize; i++) {
        String name = world().turtlesOwnNameAt(i);
        int oldpos = world().oldTurtlesOwnIndexOf(name);
        if (oldpos == -1) {
          variables()[i] = World.ZERO;
        } else {
          variables()[i] = oldvars[oldpos];
          oldvars[oldpos] = null;
        }
      }
    }

    // stage 3: handle the BREED-own variables
    for (int i = turtlesOwnSize; i < variables().length; i++) {
      String name = world().breedsOwnNameAt(getBreed(), i);
      int oldpos = compiling ? world().oldBreedsOwnIndexOf(getBreed(), name)
          : world().breedsOwnIndexOf(oldBreed, name);
      if (oldpos == -1) {
        variables()[i] = World.ZERO;
      } else {
        variables()[i] = oldvars[oldpos];
        oldvars[oldpos] = null;
      }
    }
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
    drawJumpLine(xcor, ycor, distance);
    xandycor(xcor + (distance * cachedSine),
        ycor + (distance * cachedCosine), true);
  }

  public Patch getPatchAtHeadingAndDistance(double delta, double distance)
      throws AgentException {
    double h = heading + delta;
    if (h < 0 || h >= 360) {
      h = ((h % 360) + 360) % 360;
    }
    return world().protractor().getPatchAtHeadingAndDistance(this, h, distance);
  }

  public Patch getPatchHere() {
    if (currentPatch == null) {
      //turtles cannot leave the world, so xcor and ycor will always be valid
      //so assume we dont have to access the Topologies
      currentPatch = world().getPatchAtWrap(xcor, ycor);
    }
    return currentPatch;
  }

  private void mustOwn(String name)
      throws AgentException {
    if (name != null && !world().breedOwns(getBreed(), name)) {
      throw new AgentException(I18N.errorsJ().getN("org.nlogo.agent.Agent.breedDoesNotOwnVariable",
          getBreed().printName(), name));
    }
  }


  @Override
  public Object getVariable(int vn) {
    return getTurtleVariable(vn);
  }

  public String variableName(int vn) {
    if (vn < world().program().turtlesOwn().size()) {
      return world().turtlesOwnNameAt(vn);
    } else {
      return world().breedsOwnNameAt(getBreed(), vn);
    }
  }

  @Override
  public void setVariable(int vn, Object value)
      throws AgentException {
    setTurtleVariable(vn, value);
  }

  @Override
  public Object getTurtleOrLinkVariable(String varName) {
    return getTurtleVariable(world().program().turtlesOwn().indexOf(varName));
  }

  @Override
  public Object getTurtleVariable(int vn) {
    if (vn == VAR_WHO) {
      if (variables()[VAR_WHO] == null) {
        variables()[VAR_WHO] = Double.valueOf(id());
      }
    } else if (vn == VAR_HEADING) {
      if (variables()[VAR_HEADING] == null) {
        variables()[VAR_HEADING] = Double.valueOf(heading);
      }
    } else if (vn == VAR_XCOR) {
      if (variables()[VAR_XCOR] == null) {
        variables()[VAR_XCOR] = Double.valueOf(xcor);
      }
    } else if (vn == VAR_YCOR && variables()[VAR_YCOR] == null) {
      variables()[VAR_YCOR] = Double.valueOf(ycor);
    }

    return variables()[vn];
  }

  public double getTurtleVariableDouble(int vn) {
    switch (vn) {
      case VAR_WHO:
        return id();
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
    mustOwn(name);
    int vn = world().breedsOwnIndexOf(getBreed(), name);
    return getTurtleVariable(vn);
  }

  public boolean ownsVariable(String name) {
    int vn = world().breedsOwnIndexOf(getBreed(), name);
    if (vn != -1) {
      return true;
    }
    vn = world().turtlesOwnIndexOf(name);
    if (vn != -1) {
      return true;
    }
    return false;
  }

  public Object getVariable(String name)
      throws AgentException {
    int vn = world().breedsOwnIndexOf(getBreed(), name);
    if (vn == -1) {
      vn = world().turtlesOwnIndexOf(name);
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
    setTurtleVariable(world().program().turtlesOwn().indexOf(varName), value);
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
      variables()[vn] = value;
    } else {
      switch (vn) {
        case VAR_COLOR:
          if (value instanceof Double) {
            colorDouble((Double) value);
          } else if (value instanceof LogoList) {
            color((LogoList) value, VAR_COLOR);
          } else {
            wrongTypeForVariable
                (AgentVariables.getImplicitTurtleVariables()[vn], Double.class, value);
          }
          break;
        case VAR_HEADING:
          if (value instanceof Double) {
            heading((Double) value);
          } else {
            wrongTypeForVariable
                (AgentVariables.getImplicitTurtleVariables()[vn], Double.class, value);
          }
          break;
        case VAR_XCOR:
          if (value instanceof Double) {
            xcor((Double) value);
          } else {
            wrongTypeForVariable
                (AgentVariables.getImplicitTurtleVariables()[vn], Double.class, value);
          }
          break;
        case VAR_YCOR:
          if (value instanceof Double) {
            ycor((Double) value);
          } else {
            wrongTypeForVariable
                (AgentVariables.getImplicitTurtleVariables()[vn], Double.class, value);
          }
          break;
        case VAR_SHAPE:
          if (value instanceof String) {
            String newShape = world().checkTurtleShapeName((String) value);
            if (newShape == null) {
              throw new AgentException
                  (I18N.errorsJ().getN("org.nlogo.agent.Agent.shapeUndefined", value));
            }
            shape(newShape);
          } else {
            wrongTypeForVariable
                (AgentVariables.getImplicitTurtleVariables()[vn], String.class, value);
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
                (AgentVariables.getImplicitTurtleVariables()[vn], Double.class, value);
          }
          break;
        case VAR_BREED:
          if (value instanceof AgentSet) {
            AgentSet breed = (AgentSet) value;
            if (breed != world().turtles() && !world().isBreed(breed)) {
              throw new AgentException(I18N.errorsJ().get("org.nlogo.agent.Turtle.cantSetBreedToNonBreedAgentSet"));
            }
            setBreed(breed);
          } else {
            wrongTypeForVariable
                (AgentVariables.getImplicitTurtleVariables()[vn], AgentSet.class, value);
          }
          break;
        case VAR_HIDDEN:
          if (value instanceof Boolean) {
            hidden(((Boolean) value).booleanValue());
          } else {
            wrongTypeForVariable
                (AgentVariables.getImplicitTurtleVariables()[vn], Boolean.class, value);
          }
          break;
        case VAR_SIZE:
          if (value instanceof Double) {
            size(((Double) value).doubleValue());
          } else {
            wrongTypeForVariable
                (AgentVariables.getImplicitTurtleVariables()[vn],
                    Double.class, value);
          }
          break;
        case VAR_PENMODE:
          if (value instanceof String) {
            penMode((String) value);
          } else {
            wrongTypeForVariable(AgentVariables.getImplicitTurtleVariables()[vn],
                String.class, value);
          }
          break;

        case VAR_PENSIZE:
          if (value instanceof Double) {
            penSize(((Double) value).doubleValue());
          } else {
            wrongTypeForVariable(AgentVariables.getImplicitTurtleVariables()[vn],
                Double.class, value);
          }
          break;

        case VAR_WHO:
          throw new AgentException(I18N.errorsJ().get("org.nlogo.agent.Turtle.cantChangeWho"));
        default:
          throw new IllegalStateException(I18N.errorsJ().getN("org.nlogo.agent.Agent.cantSetUnknownVariable", vn));
      }
    }
    world().notifyWatchers(this, vn, value);
  }

  @Override
  public void setBreedVariable(String name, Object value)
      throws AgentException {
    mustOwn(name);
    int vn = world().breedsOwnIndexOf(getBreed(), name);
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
    return variables()[VAR_COLOR];
  }

  public void colorDouble(Double boxedColor) {
    double c = boxedColor.doubleValue();
    if (c < 0 || c >= Color.MaxColor()) {
      c = Color.modulateDouble(c);
      boxedColor = Double.valueOf(c);
    }
    variables()[VAR_COLOR] = boxedColor;
  }

  public void colorDoubleUnchecked(Double boxedColor) {
    variables()[VAR_COLOR] = boxedColor;
  }

  public void color(LogoList rgb, int varIndex)
      throws AgentException {
    org.nlogo.api.Color.validRGBList(rgb, true);
    variables()[varIndex] = rgb;
    if(rgb.size() > 3) {
      world().mayHavePartiallyTransparentObjects = true;
    }
  }

  public void turnRight(double delta) {
    heading(heading + delta);
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
    if (world().tieManager.tieCount > 0) {
      world().tieManager.turtleTurned(this, heading, originalHeading);
    }
  }

  // when we're calculating ties this gets called since we are recursing
  // and we need to pass the seenTurtles around ev 7/24/07
  public void heading(double heading, Set<Turtle> seenTurtles) {
    double originalHeading = this.heading;
    headingHelper(heading);
    if (world().tieManager.tieCount > 0) {
      world().tieManager.turtleTurned(this, heading, originalHeading, seenTurtles);
    }
  }

  private void headingHelper(double heading) {
    if (heading < 0 || heading >= 360) {
      heading = ((heading % 360) + 360) % 360;
    }
    this.heading = heading;
    variables()[VAR_HEADING] = null;
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
      variables()[VAR_HEADING] = heading;
    } else {
      variables()[VAR_HEADING] = null;
    }
    if (world().tieManager.tieCount > 0) {
      world().tieManager.turtleTurned(this, h, originalHeading);
    }
  }

  ///

  void drawLine(double x0, double y0, double x1, double y1) {
    if (!penMode().equals(PEN_UP) && (x0 != x1 || y0 != y1)) {
      world().drawLine(x0, y0, x1, y1, variables()[VAR_COLOR], penSize(), penMode());
    }
  }


  void drawJumpLine(double x, double y, double dist) {
    if (!penMode().equals(PEN_UP)) {
      Object color = variables()[VAR_COLOR];
      double size = penSize();
      String mode = penMode();
      double minPxcor = world().minPxcor() - 0.5;
      double maxPxcor = world().maxPxcor() + 0.5;
      double minPycor = world().minPycor() - 0.5;
      double maxPycor = world().maxPycor() + 0.5;
      Trail[] lines   = PenLineMaker.apply(x, y, heading, dist, minPxcor, maxPxcor, minPycor, maxPycor);
      for (Trail line : lines) {
        world().drawLine(line.x1(), line.y1(), line.x2(), line.y2(), color, size, mode);
      }
    }
  }

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
    if (!penMode().equals(PEN_DOWN)) {
      return y;
    }

    // wrap the coords (we do this first in case we're trying to move outside
    // the world we want to trigger the exception.)
    y = world().wrapY(y);

    double yprime;

    // find the "unwrapped" coordinates
    // could we just be undoing what we just did? yes.
    // but do we really want to try and figure that out?
    if (y > ycor) {
      yprime = y - world().worldHeight();
    } else {
      yprime = y + world().worldHeight();
    }

    // techincally we're not supposed to check these
    // directly but it seems like the clearest thing to
    // do here since we don't want to change the values
    // just make sure that we're obeying the topology if
    // we were to use one of these paths.
    if (!world().wrappingAllowedInY()) {
      yprime = y;
    }

    if (StrictMath.abs(y - ycor) > StrictMath.abs(yprime - ycor)) {
      y = yprime;
    }

    return y;
  }

  public double shortestPathX(double x)
      throws AgentException {
    if (!penMode().equals(PEN_DOWN)) {
      return x;
    }

    x = world().wrapX(x);

    double xprime;

    if (x > xcor) {
      xprime = x - world().worldWidth();
    } else {
      xprime = x + world().worldWidth();
    }

    if (!world().wrappingAllowedInX()) {
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

    this.xcor = world().wrapX(xcor);

    variables()[VAR_XCOR] = null;
    currentPatch = null;
    Patch targetPatch = getPatchHere();
    if (originalPatch != targetPatch) {
      originalPatch.removeTurtle(this);
      targetPatch.addTurtle(this);
    }
    if (world().tieManager.tieCount > 0) {
      world().tieManager.turtleMoved
          (this, xcor, ycor, oldX, ycor);
    }
  }

  public void xcor(Double xcor)
      throws AgentException {
    Patch originalPatch = getPatchHere();
    double x = xcor.doubleValue();
    double oldX = this.xcor;

    double wrapped = world().wrapX(x);

    drawLine(this.xcor, ycor, shortestPathX(x), ycor);

    this.xcor = wrapped;
    if (x == wrapped) {
      variables()[VAR_XCOR] = xcor;
    } else {
      variables()[VAR_XCOR] = null;
    }
    currentPatch = null;
    Patch targetPatch = getPatchHere();
    if (originalPatch != targetPatch) {
      originalPatch.removeTurtle(this);
      targetPatch.addTurtle(this);
    }
    if (world().tieManager.tieCount > 0) {
      world().tieManager.turtleMoved
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

    this.ycor = world().wrapY(ycor);

    variables()[VAR_YCOR] = null;
    currentPatch = null;
    Patch targetPatch = getPatchHere();
    if (originalPatch != targetPatch) {
      originalPatch.removeTurtle(this);
      targetPatch.addTurtle(this);
    }
    if (world().tieManager.tieCount > 0) {
      world().tieManager.turtleMoved(this, xcor, ycor, xcor, oldY);
    }
  }

  public void ycor(Double ycor)
      throws AgentException {
    Patch originalPatch = getPatchHere();
    double y = ycor.doubleValue();
    double oldY = this.ycor;
    double wrapped = world().wrapY(y);

    drawLine(xcor, this.ycor, xcor, shortestPathY(y));

    this.ycor = wrapped;
    if (y == wrapped) {
      variables()[VAR_YCOR] = ycor;
    } else {
      variables()[VAR_YCOR] = null;
    }
    currentPatch = null;
    Patch targetPatch = getPatchHere();
    if (originalPatch != targetPatch) {
      originalPatch.removeTurtle(this);
      targetPatch.addTurtle(this);
    }
    if (world().tieManager.tieCount > 0) {
      world().tieManager.turtleMoved(this, xcor, y, xcor, oldY);
    }
  }

  public void xandycor(double xcor, double ycor)
      throws AgentException {
    xandycor(xcor, ycor, false);
  }

  void xandycor(double xcor, double ycor, boolean isJumping)
      throws AgentException {
    double oldX = this.xcor;
    double oldY = this.ycor;
    xandycorHelper(xcor, ycor, isJumping);
    if (world().tieManager.tieCount > 0) {
      world().tieManager.turtleMoved(this, xcor, ycor, oldX, oldY);
    }
  }

  // when we're calculating ties this gets called since we are recursing
  // and we need to pass the seenTurtles around ev 7/24/07
  public void xandycor(double xcor, double ycor, Set<Turtle> seenTurtles)
      throws AgentException {
    double oldX = this.xcor;
    double oldY = this.ycor;
    xandycorHelper(xcor, ycor, false);
    if (world().tieManager.tieCount > 0) {
      world().tieManager.turtleMoved(this, xcor, ycor, oldX, oldY, seenTurtles);
    }
  }

  public void xandycorHelper(double xcor, double ycor, boolean isJumping)
      throws AgentException {
    Patch originalPatch = getPatchHere();

    double newX = world().wrapX(xcor);
    double newY = world().wrapY(ycor);

    if (!isJumping) {
      drawLine(this.xcor, this.ycor, xcor, ycor);
    }

    this.xcor = newX;
    this.ycor = newY;

    variables()[VAR_XCOR] = null;
    variables()[VAR_YCOR] = null;
    currentPatch = null;
    Patch targetPatch = getPatchHere();
    if (originalPatch != targetPatch) {
      originalPatch.removeTurtle(this);
      targetPatch.addTurtle(this);
    }
  }

  public void xandycor(Double xcor, Double ycor)
      throws AgentException {
    Patch originalPatch = getPatchHere();
    double x = xcor.doubleValue();
    double y = ycor.doubleValue();

    double oldX = this.xcor;
    double oldY = this.ycor;

    double wrappedX = world().wrapX(x);
    double wrappedY = world().wrapY(y);

    drawLine(this.xcor, this.ycor, x, y);

    this.xcor = wrappedX;
    this.ycor = wrappedY;
    variables()[VAR_XCOR] = (x == wrappedX) ? xcor : null;
    variables()[VAR_YCOR] = (y == wrappedY) ? ycor : null;
    currentPatch = null;
    Patch targetPatch = getPatchHere();
    if (originalPatch != targetPatch) {
      originalPatch.removeTurtle(this);
      targetPatch.addTurtle(this);
    }
    if (world().tieManager.tieCount > 0) {
      world().tieManager.turtleMoved(this, x, y, oldX, oldY);
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
      variables()[VAR_XCOR] = p.variables()[Patch.VAR_PXCOR];
      variables()[VAR_YCOR] = p.variables()[Patch.VAR_PYCOR];
      if (world().tieManager.tieCount > 0) {
        world().tieManager.turtleMoved(this, x, y, oldX, oldY);
      }
    }
  }

  public void face(Agent agent, boolean wrap) {
    try {
      heading(world().protractor().towards(this, agent, wrap));
    } catch (AgentException ex) {
      // AgentException here means we tried to calculate the heading from
      // an agent to itself, or to an agent at the exact same position.
      // Since face is nice, it just ignores the exception and doesn't change
      // the callers heading. - AZS 6/22/05
      org.nlogo.api.Exceptions.ignore(ex);
    }
  }

  public void face(double x, double y, boolean wrap) {
    try {
      heading(world().protractor().towards(this, x, y, wrap));
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

  public void home() {
    try {
      xandycor(World.ZERO, World.ZERO);
    } catch (AgentException e) {
      // this will never happen since we require 0,0 be inside the world.
      throw new IllegalStateException(e);
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

  public String shape() {
    return (String) variables()[VAR_SHAPE];
  }

  public void shape(String shape) {
    variables()[VAR_SHAPE] = shape;
  }

  public Object label() {
    return variables()[VAR_LABEL];
  }

  public boolean hasLabel() {
    return !(label() instanceof String &&
        ((String) label()).length() == 0);
  }

  public String labelString() {
    return Dump.logoObject(variables()[VAR_LABEL]);
  }

  public void label(Object label) {
    variables()[VAR_LABEL] = label;
  }

  public Object labelColor() {
    return variables()[VAR_LABELCOLOR];
  }

  public void labelColor(double labelColor) {
    variables()[VAR_LABELCOLOR] = Double.valueOf(Color.modulateDouble(labelColor));
  }

  public void labelColor(LogoList rgb, int valueIndex)
      throws AgentException {
    org.nlogo.api.Color.validRGBList(rgb, true);
    variables()[valueIndex] = rgb;
  }

  public AgentSet getBreed() {
    return (AgentSet) variables()[VAR_BREED];
  }

  // returns the index of the breed of this turtle, 0 means just a turtle
  // this is super kludge. is there a better way? -AZS 10/28/04
  public int getBreedIndex() {
    AgentSet mybreed = getBreed();
    if (mybreed == world().turtles()) {
      return 0;
    }
    int j = 0;
    scala.collection.Iterator<String> iter =
      world().program().breeds().keys().iterator();
    while(iter.hasNext()) {
      if (world().breedAgents.get(iter.next()) == mybreed) {
        return j;
      }
      j++;
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
    TreeAgentSet oldBreed = null;
    if (variables()[VAR_BREED] instanceof AgentSet) {
      oldBreed = (TreeAgentSet) variables()[VAR_BREED];
      if (breed == oldBreed) {
        return;
      }
      if (oldBreed != world().turtles()) {
        oldBreed.remove(agentKey());
      }
    }
    if (breed != world().turtles()) {
      ((TreeAgentSet) breed).add(this);
    }
    variables()[VAR_BREED] = breed;
    shape(world().turtleBreedShapes.breedShape(breed));
    realloc(false, oldBreed);
  }

  public boolean hidden() {
    return ((Boolean) variables()[VAR_HIDDEN]).booleanValue();
  }

  public void hidden(boolean hidden) {
    variables()[VAR_HIDDEN] = hidden ? Boolean.TRUE : Boolean.FALSE;
  }

  public double size() {
    return ((Double) variables()[VAR_SIZE]).doubleValue();
  }

  public void size(double size) {
    variables()[VAR_SIZE] = Double.valueOf(size);
  }

  public double penSize() {
    return ((Double) variables()[VAR_PENSIZE]).doubleValue();
  }

  public void penSize(double penSize) {
    variables()[VAR_PENSIZE] = Double.valueOf(penSize);
  }

  public String penMode() {
    return (String) variables()[VAR_PENMODE];
  }

  public void penMode(String penMode) {
    variables()[VAR_PENMODE] = penMode;
  }

  @Override
  public String toString() {
    return world().getBreedSingular(getBreed()).toLowerCase() + " " + id();
  }

  @Override
  public String classDisplayName() {
    return world().getBreedSingular(getBreed()).toLowerCase();
  }

  public static final int BIT = AgentBit.apply(AgentKindJ.Turtle());

  @Override
  public int agentBit() {
    return BIT;
  }

  public int alpha() {
    return org.nlogo.api.Color.getColor(color()).getAlpha();
  }


}
