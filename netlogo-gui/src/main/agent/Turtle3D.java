// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

import org.nlogo.api.AgentException;
import org.nlogo.api.AgentVariableNumbers;
import org.nlogo.api.AgentVariables;
import org.nlogo.api.Color;
import org.nlogo.api.Dump;
import org.nlogo.core.I18N;
import org.nlogo.core.LogoList;
import org.nlogo.api.Vect;

import java.util.List;

public final strictfp class Turtle3D
    extends Turtle
    implements Agent3D, org.nlogo.api.Turtle3D {
  public static final int VAR_WHO3D = AgentVariableNumbers.VAR_WHO3D;
  public static final int VAR_COLOR3D = AgentVariableNumbers.VAR_COLOR3D;
  public static final int VAR_HEADING3D = AgentVariableNumbers.VAR_HEADING3D;
  public static final int VAR_PITCH3D = AgentVariableNumbers.VAR_PITCH3D;
  public static final int VAR_ROLL3D = AgentVariableNumbers.VAR_ROLL3D;
  public static final int VAR_XCOR3D = AgentVariableNumbers.VAR_XCOR3D;
  public static final int VAR_YCOR3D = AgentVariableNumbers.VAR_YCOR3D;
  public static final int VAR_ZCOR3D = AgentVariableNumbers.VAR_ZCOR3D;
  static final int VAR_SHAPE3D = AgentVariableNumbers.VAR_SHAPE3D;
  public static final int VAR_LABEL3D = AgentVariableNumbers.VAR_LABEL3D;
  private static final int VAR_LABELCOLOR3D = AgentVariableNumbers.VAR_LABELCOLOR3D;
  static final int VAR_BREED3D = AgentVariableNumbers.VAR_BREED3D;
  private static final int VAR_HIDDEN3D = AgentVariableNumbers.VAR_HIDDEN3D;
  private static final int VAR_SIZE3D = AgentVariableNumbers.VAR_SIZE3D;
  private static final int VAR_PENSIZE3D = AgentVariableNumbers.VAR_PENSIZE3D;
  private static final int VAR_PENMODE3D = AgentVariableNumbers.VAR_PENMODE3D;

  void initvars(Number xcor, Number ycor, AgentSet breed) {
    LAST_PREDEFINED_VAR = VAR_PENMODE3D;
    NUMBER_PREDEFINED_VARS = LAST_PREDEFINED_VAR + 1;

    variables[VAR_COLOR3D] = Color.BoxedBlack();
    heading = 0;
    variables[VAR_HEADING3D] = World.ZERO;
    this.xcor = xcor.doubleValue();
    if (xcor instanceof Double) {
      variables[VAR_XCOR3D] = xcor;
    }
    this.ycor = ycor.doubleValue();
    if (ycor instanceof Double) {
      variables[VAR_YCOR3D] = ycor;
    }
    variables[VAR_SHAPE3D] = world.turtleBreedShapes.breedShape(breed);
    variables[VAR_LABEL3D] = "";
    variables[VAR_LABELCOLOR3D] = Color.BoxedWhite();
    variables[VAR_BREED3D] = breed;
    variables[VAR_HIDDEN3D] = Boolean.FALSE;
    variables[VAR_SIZE3D] = World.ONE;
    variables[VAR_PENSIZE3D] = World.ONE;
    variables[VAR_PENMODE3D] = PEN_UP;
  }

  public Turtle3D(World3D world, AgentSet breed, Number xcor, Number ycor, Number zcor) {
    this(world, breed, xcor, ycor, zcor, true);
  }

  private Turtle3D(World3D world, AgentSet breed, Number xcor, Number ycor, Number zcor, boolean getId) {
    super(world);

    variables = new Object[world.getVariablesArraySize(this, breed)];
    if (getId) {
      id(world.newTurtleId());
      world.turtles().add(this);
    }
    initvars(xcor, ycor, breed);

    for (int i = LAST_PREDEFINED_VAR + 1; i < variables.length; i++) {
      variables[i] = World.ZERO;
    }
    if (breed != world.turtles()) {
      ((TreeAgentSet) breed).add(this);
    }

    variables[VAR_PITCH3D] = World.ZERO;
    variables[VAR_ROLL3D] = World.ZERO;
    this.zcor = zcor.doubleValue();
    if (zcor instanceof Double) {
      variables[VAR_ZCOR3D] = zcor;
    }
    getPatchHere().addTurtle(this);
  }

  Turtle3D(World world) {
    super(world);
  }

  Turtle3D(World world, long id) {
    this((World3D) world, world.turtles(),
        World.ZERO, World.ZERO, World.ZERO,
        false);
    id(id);
    world.turtles().add(this);
  }

  @Override
  public Turtle hatch(AgentSet breed) {
    Turtle3D child = new Turtle3D(world);
    child.heading = heading;
    child.xcor = xcor;
    child.ycor = ycor;
    child.zcor = zcor;
    child.variables = variables.clone();
    child.id(world.newTurtleId());
    world.turtles().add(child);
    if (breed != world.turtles()) {
      ((TreeAgentSet) breed).add(child);
    }
    child.getPatchHere().addTurtle(child);
    return child;
  }

  @Override
  public Patch getPatchAtOffsets(double dx, double dy)
      throws AgentException {
    Patch target = ((World3D) world).getPatchAt(xcor + dx, ycor + dy, zcor);
    if (target == null) {
      throw new AgentException("Cannot get patch beyond limits of current world.");
    }
    return target;
  }

  public Patch3D getPatchAtOffsets(double dx, double dy, double dz)
      throws AgentException {
    Patch3D target = ((World3D) world).getPatchAt(xcor + dx, ycor + dy, zcor + dz);
    if (target == null) {
      throw new AgentException("Cannot get patch beyond limits of current world.");
    }
    return target;
  }

  public Patch getPatchAtPoint(List<Double> point)
      throws AgentException {
    double dx = point.get(0).doubleValue();
    double dy = point.get(1).doubleValue();
    double dz = point.size() == 3 ? point.get(2).doubleValue() : 0;
    return getPatchAtOffsets(dx, dy, dz);
  }

  // note this is very similar to
  // World.getPatchAtDistanceAndHeading() - ST 9/3/03
  @Override
  public void jump(double distance) {
    double pitchRadians = StrictMath.toRadians(pitch());
    double sin = StrictMath.sin(pitchRadians);
    double distProj = distance * StrictMath.cos(pitchRadians);
    if (StrictMath.abs(sin) < org.nlogo.api.Constants.Infinitesimal()) {
      sin = 0;
    }
    if (StrictMath.abs(distProj) < org.nlogo.api.Constants.Infinitesimal()) {
      distProj = 0;
    }

    double headingRadians = StrictMath.toRadians(heading());
    double cosProj = StrictMath.cos(headingRadians);
    double sinProj = StrictMath.sin(headingRadians);

    if (StrictMath.abs(cosProj) < org.nlogo.api.Constants.Infinitesimal()) {
      cosProj = 0;
    }
    if (StrictMath.abs(sinProj) < org.nlogo.api.Constants.Infinitesimal()) {
      sinProj = 0;
    }

    xyandzcor(xcor + (distProj * sinProj),
        ycor + (distProj * cosProj),
        zcor + (distance * sin));

  }

  @Override
  public Patch getPatchHere() {
    if (currentPatch == null) {
      //turtles cannot leave the world, so xcor and ycor will always be valid
      //so assume we dont have to access the Topologies
      currentPatch = ((World3D) world).getPatchAtWrap(xcor, ycor, zcor);
    }
    return currentPatch;
  }

  @Override
  public Object getTurtleVariable(int vn) {
    switch (vn) {
      case VAR_WHO3D:
        if (variables[VAR_WHO3D] == null) {
          variables[VAR_WHO3D] = Double.valueOf(id);
        }
        break;
      case VAR_HEADING3D:
        if (variables[VAR_HEADING3D] == null) {
          variables[VAR_HEADING3D] = Double.valueOf(heading);
        }
        break;
      case VAR_XCOR3D:
        if (variables[VAR_XCOR3D] == null) {
          variables[VAR_XCOR3D] = Double.valueOf(xcor);
        }
        break;
      case VAR_YCOR3D:
        if (variables[VAR_YCOR3D] == null) {
          variables[VAR_YCOR3D] = Double.valueOf(ycor);
        }
        break;
      case VAR_ZCOR3D:
        if (variables[VAR_ZCOR3D] == null) {
          variables[VAR_ZCOR3D] = Double.valueOf(zcor);
        }
        break;
      default:
        break;
    }
    return variables[vn];
  }

  @Override
  public double getTurtleVariableDouble(int vn) {
    switch (vn) {
      case VAR_HEADING3D:
        return heading;
      case VAR_PITCH3D:
        return pitch();
      case VAR_ROLL3D:
        return roll();
      case VAR_XCOR3D:
        return xcor;
      case VAR_YCOR3D:
        return ycor;
      case VAR_ZCOR3D:
        return zcor;
      case VAR_SIZE3D:
        return size();
      case VAR_PENSIZE3D:
        return penSize();
      default:
        throw new IllegalArgumentException
            (vn + " is not a double variable");
    }
  }

  @Override
  public void setTurtleVariable(int vn, double value)
      throws AgentException {
    switch (vn) {
      case VAR_HEADING3D:
        heading(value);
        break;
      case VAR_XCOR3D:
        xcor(value);
        break;
      case VAR_YCOR3D:
        ycor(value);
        break;
      case VAR_SIZE3D:
        size(value);
        break;
      case VAR_PENSIZE3D:
        penSize(value);
        break;
      case VAR_PITCH3D:
        pitch(value);
        break;
      case VAR_ROLL3D:
        roll(value);
        break;
      case VAR_ZCOR3D:
        zcor(value);
        break;
      case VAR_WHO3D:
        throw new AgentException("you can't change a turtle's who number");
      default:
        throw new IllegalArgumentException
            (vn + " is not a double variable");
    }
  }

  @Override
  public void setTurtleVariable(int vn, Object value)
      throws AgentException {
    if (vn > LAST_PREDEFINED_VAR) {
      variables[vn] = value;
    } else {
      switch (vn) {
        case VAR_COLOR3D:
          if (value instanceof Double) {
            colorDouble((Double) value);
          } else if (value instanceof LogoList) {
            color((LogoList) value, VAR_COLOR3D);
          } else {
            wrongTypeForVariable(AgentVariables.getImplicitTurtleVariables(true)[vn],
                Double.class, value);
          }
          break;
        case VAR_HEADING3D:
          if (value instanceof Double) {
            heading((Double) value);
          } else {
            wrongTypeForVariable(AgentVariables.getImplicitTurtleVariables(true)[vn],
                Double.class, value);
          }
          break;
        case VAR_XCOR3D:
          if (value instanceof Double) {
            xcor((Double) value);
          } else {
            wrongTypeForVariable(AgentVariables.getImplicitTurtleVariables(true)[vn],
                Double.class, value);
          }
          break;
        case VAR_YCOR3D:
          if (value instanceof Double) {
            ycor((Double) value);
          } else {
            wrongTypeForVariable(AgentVariables.getImplicitTurtleVariables(true)[vn],
                Double.class, value);
          }
          break;
        case VAR_SHAPE3D:
          if (value instanceof String) {
            String newShape = world.checkTurtleShapeName((String) value);
            if (newShape == null) {
              throw new AgentException
                  ("\"" + (String) value + "\" is not a currently defined shape");
            }
            shape(newShape);
          } else {
            wrongTypeForVariable(AgentVariables.getImplicitTurtleVariables(true)[vn],
                String.class, value);
          }
          break;
        case VAR_LABEL3D:
          label(value);
          break;
        case VAR_LABELCOLOR3D:
          if (value instanceof Number) {
            labelColor(((Number) value).doubleValue());
          } else if (value instanceof LogoList) {
            labelColor((LogoList) value, VAR_LABELCOLOR3D);
          } else {
            wrongTypeForVariable(AgentVariables.getImplicitTurtleVariables(true)[vn],
                Double.class, value);
          }
          break;
        case VAR_BREED3D:
          if (value instanceof AgentSet) {
            AgentSet breed = (AgentSet) value;
            if (breed != world.turtles() && !world.isBreed(breed)) {
              throw new AgentException(I18N.errorsJ().get("org.nlogo.agent.Turtle.cantSetBreedToNonBreedAgentSet"));
            }
            setBreed(breed);
          } else {
            wrongTypeForVariable(AgentVariables.getImplicitTurtleVariables(true)[vn],
                AgentSet.class, value);
          }
          break;
        case VAR_HIDDEN3D:
          if (value instanceof Boolean) {
            hidden(((Boolean) value).booleanValue());
          } else {
            wrongTypeForVariable(AgentVariables.getImplicitTurtleVariables(true)[vn],
                Boolean.class, value);
          }
          break;
        case VAR_SIZE3D:
          if (value instanceof Number) {
            size(((Number) value).doubleValue());
          } else {
            wrongTypeForVariable(AgentVariables.getImplicitTurtleVariables(true)[vn],
                Double.class, value);
          }
          break;
        case VAR_PENMODE3D:
          if (value instanceof String) {
            penMode((String) value);
          } else {
            wrongTypeForVariable(AgentVariables.getImplicitTurtleVariables(true)[vn],
                String.class, value);
          }
          break;

        case VAR_PENSIZE3D:
          if (value instanceof Number) {
            penSize(((Number) value).doubleValue());
          } else {
            wrongTypeForVariable(AgentVariables.getImplicitTurtleVariables(true)[vn],
                Double.class, value);
          }
          break;

        case VAR_WHO3D:
          throw new AgentException("you can't change a turtle's ID number");

        case VAR_PITCH3D:
          if (value instanceof Number) {
            pitch(((Number) value).doubleValue());
          } else {
            wrongTypeForVariable(AgentVariables.getImplicitTurtleVariables(true)[vn],
                Double.class, value);
          }
          break;
        case VAR_ROLL3D:
          if (value instanceof Number) {
            roll(((Number) value).doubleValue());
          } else {
            wrongTypeForVariable(AgentVariables.getImplicitTurtleVariables(true)[vn],
                Double.class, value);
          }
          break;
        case VAR_ZCOR3D:
          if (value instanceof Double) {
            zcor((Double) value);
          } else {
            wrongTypeForVariable(AgentVariables.getImplicitTurtleVariables(true)[vn],
                Double.class, value);
          }
          break;
        default:
          return;
      }
    }
  }

  public double pitch() {
    return ((Double) variables[VAR_PITCH3D]).doubleValue();
  }

  public void pitch(double pitch) {
    double originalPitch = this.pitch();
    if (pitch < 0 || pitch >= 360) {
      pitch = ((pitch % 360) + 360) % 360;
    }
    variables[VAR_PITCH3D] = Double.valueOf(pitch);
    if (this == world.observer().targetAgent()) {
      world.observer().updatePosition();
    }
    if (world.tieManager.hasTies()) {
      turtleOrientationChanged(heading, pitch, this.roll(),
          heading, originalPitch, this.roll());
    }
  }

  public double roll() {
    return ((Double) variables[VAR_ROLL3D]).doubleValue();
  }

  public void roll(double roll) {
    double originalRoll = this.roll();
    if (roll < 0 || roll >= 360) {
      roll = ((roll % 360) + 360) % 360;
    }
    variables[VAR_ROLL3D] = Double.valueOf(roll);
    if (this == world.observer().targetAgent()) {
      world.observer().updatePosition();
    }
    if (world.tieManager.hasTies()) {
      turtleOrientationChanged(this.heading(), this.pitch(), roll,
          this.heading(), this.pitch(), originalRoll);
    }
  }

  public void headingPitchAndRoll(double heading, double pitch, double roll) {
    double originalHeading = this.heading();
    double originalPitch = this.pitch();
    double originalRoll = this.roll();

    if (roll < 0 || roll >= 360) {
      roll = ((roll % 360) + 360) % 360;
    }
    if (pitch < 0 || pitch >= 360) {
      pitch = ((pitch % 360) + 360) % 360;
    }
    if (heading < 0 || heading >= 360) {
      heading = ((heading % 360) + 360) % 360;
    }
    variables[VAR_PITCH3D] = Double.valueOf(pitch);
    variables[VAR_ROLL3D] = Double.valueOf(roll);
    this.heading = heading;
    variables[VAR_HEADING] = null;
    if (this == world.observer().targetAgent()) {
      world.observer().updatePosition();
    }
    if (world.tieManager.hasTies()) {
      turtleOrientationChanged(heading, pitch, roll,
          originalHeading, originalPitch, originalRoll);
    }
  }

  @Override
  void drawLine(double x0, double y0, double x1, double y1) {
    if (penMode().equals(PEN_DOWN) && (x0 != x1 || y0 != y1)) {
      ((World3D) world).drawLine
          (x0, y0, zcor, x1, y1, zcor, color(), penSize());
    }
  }

  void drawLine(double x0, double y0, double z0, double x1, double y1, double z1) {
    if (penMode().equals(PEN_DOWN) && (x0 != x1 || y0 != y1 || z0 != z1)) {
      ((World3D) world).drawLine(x0, y0, z0, x1, y1, z1, color(), penSize());
    }
  }

  public double shortestPathZ(double z) {
    if (!penMode().equals(PEN_DOWN)) {
      return z;
    }

    World3D w = (World3D) world;

    z = ((Topology3D) world.getTopology()).wrapZ(z);

    double zprime;

    if (z > zcor) {
      zprime = z - w.worldDepth();
    } else {
      zprime = z + w.worldDepth();
    }

    if (StrictMath.abs(z - zcor) > StrictMath.abs(zprime - zcor)) {
      z = zprime;
    }

    return z;
  }

  @Override
  public void moveTo(Agent otherAgent)
      throws AgentException {
    double x, y, z;
    if (otherAgent instanceof Turtle) {
      Turtle3D t = (Turtle3D) otherAgent;
      x = t.xcor();
      y = t.ycor();
      z = t.zcor();
    } else {
      Patch3D p = (Patch3D) otherAgent;
      x = p.pxcor;
      y = p.pycor;
      z = p.pzcor;
    }

    xyandzcor(shortestPathX(x), shortestPathY(y), shortestPathZ(z));
  }

  @Override
  public void moveToPatchCenter() {
    Patch3D p = (Patch3D) getPatchHere();
    double x = p.pxcor;
    double y = p.pycor;
    double z = p.pzcor;
    double oldX = this.xcor;
    double oldY = this.ycor;
    double oldZ = this.zcor;
    drawLine(oldX, oldY, oldZ, x, y, z);
    if (x != oldX || y != oldY || z != oldZ) {
      this.xcor = x;
      this.ycor = y;
      this.zcor = z;
      variables[VAR_XCOR3D] = p.variables[Patch3D.VAR_PXCOR3D];
      variables[VAR_YCOR3D] = p.variables[Patch3D.VAR_PYCOR3D];
      variables[VAR_ZCOR3D] = p.variables[Patch3D.VAR_PZCOR3D];
      Observer observer = world.observer();
      if (this == observer.targetAgent()) {
        observer.updatePosition();
      }
      if (world.tieManager.hasTies()) {
        ((TieManager3D) world.tieManager).turtleMoved(this, x, y, z, oldX, oldY, oldZ);
      }
    }
  }

  @Override
  public void xcor(double xcor)
      throws AgentException {
    Patch originalPatch = getPatchHere();

    double oldX = this.xcor;

    drawLine(oldX, ycor, shortestPathX(xcor), ycor);

    this.xcor = world.getTopology().wrapX(xcor);

    variables[VAR_XCOR3D] = null;
    currentPatch = null;
    Patch targetPatch = getPatchHere();
    if (originalPatch != targetPatch) {
      originalPatch.removeTurtle(this);
      targetPatch.addTurtle(this);
    }
    Observer observer = world.observer();
    if (this == observer.targetAgent()) {
      observer.updatePosition();
    }
    if (world.tieManager.hasTies()) {
      turtleMoved(xcor, ycor, zcor, oldX, ycor, zcor);
    }
  }

  @Override
  public void xcor(Double xcor)
      throws AgentException {
    Patch originalPatch = getPatchHere();
    double x = xcor.doubleValue();
    double wrapped = world.getTopology().wrapX(x);
    double oldX = this.xcor;

    drawLine(this.xcor, ycor, shortestPathX(x), ycor);

    this.xcor = wrapped;
    if (x == wrapped) {
      variables[VAR_XCOR3D] = xcor;
    } else {
      variables[VAR_XCOR3D] = null;
    }
    currentPatch = null;
    Patch targetPatch = getPatchHere();
    if (originalPatch != targetPatch) {
      originalPatch.removeTurtle(this);
      targetPatch.addTurtle(this);
    }
    Observer observer = world.observer();
    if (this == observer.targetAgent()) {
      observer.updatePosition();
    }

    if (world.tieManager.hasTies()) {
      turtleMoved(x, ycor, zcor, oldX, ycor, zcor);
    }
  }

  double zcor;

  public double zcor() {
    return zcor;
  }

  public void zcor(double zcor) {
    Patch originalPatch = getPatchHere();
    double oldZ = this.zcor;
    World3D w = (World3D) world;
    double z = Topology.wrap(zcor, w.minPzcor() - 0.5, w.maxPzcor() + 0.5);

    drawLine(xcor, ycor, this.zcor, xcor, ycor, shortestPathZ(zcor));

    this.zcor = z;

    variables[VAR_ZCOR3D] = null;
    currentPatch = null;
    Patch targetPatch = getPatchHere();
    if (originalPatch != targetPatch) {
      originalPatch.removeTurtle(this);
      targetPatch.addTurtle(this);
    }
    if (world.tieManager.hasTies()) {
      turtleMoved(xcor, ycor, zcor, xcor, ycor, oldZ);
    }
  }

  public void zcor(Double zcor) {
    Patch originalPatch = getPatchHere();
    double oldZ = this.zcor;
    double z = zcor.doubleValue();
    World3D w = (World3D) world;
    double wrapped = Topology.wrap(zcor.doubleValue(), w.minPzcor() - 0.5, w.maxPzcor() + 0.5);

    drawLine(xcor, ycor, this.zcor, xcor, ycor, shortestPathZ(z));

    this.zcor = wrapped;

    if (z == wrapped) {
      variables[VAR_ZCOR3D] = zcor;
    } else {
      variables[VAR_ZCOR3D] = null;
    }
    currentPatch = null;
    Patch targetPatch = getPatchHere();
    if (originalPatch != targetPatch) {
      originalPatch.removeTurtle(this);
      targetPatch.addTurtle(this);
    }
    if (world.tieManager.hasTies()) {
      turtleMoved(xcor, ycor, z, xcor, ycor, oldZ);
    }
  }

  @Override
  public void ycor(double ycor)
      throws AgentException {
    Patch originalPatch = getPatchHere();

    double oldY = this.ycor;

    drawLine(xcor, oldY, xcor, shortestPathY(ycor));

    this.ycor = world.getTopology().wrapY(ycor);

    variables[VAR_YCOR3D] = null;
    currentPatch = null;
    Patch targetPatch = getPatchHere();
    if (originalPatch != targetPatch) {
      originalPatch.removeTurtle(this);
      targetPatch.addTurtle(this);
    }
    Observer observer = world.observer();
    if (this == observer.targetAgent()) {
      observer.updatePosition();
    }
    if (world.tieManager.hasTies()) {
      turtleMoved(xcor, ycor, zcor, xcor, oldY, zcor);
    }
  }

  @Override
  public void ycor(Double ycor)
      throws AgentException {
    Patch originalPatch = getPatchHere();
    double oldY = this.ycor;
    double y = ycor.doubleValue();
    double wrapped = world.getTopology().wrapY(y);

    drawLine(xcor, this.ycor, xcor, shortestPathY(y));

    this.ycor = wrapped;
    if (y == wrapped) {
      variables[VAR_YCOR3D] = ycor;
    } else {
      variables[VAR_YCOR3D] = null;
    }
    currentPatch = null;
    Patch targetPatch = getPatchHere();
    if (originalPatch != targetPatch) {
      originalPatch.removeTurtle(this);
      targetPatch.addTurtle(this);
    }
    Observer observer = world.observer();
    if (this == observer.targetAgent()) {
      observer.updatePosition();
    }
    if (world.tieManager.hasTies()) {
      turtleMoved(xcor, y, zcor, xcor, oldY, zcor);
    }
  }

  @Override
  public void xandycor(double xcor, double ycor)
      throws AgentException {
    Patch originalPatch = getPatchHere();
    double oldX = this.xcor;
    double oldY = this.ycor;
    double newX = world.getTopology().wrapX(xcor);
    double newY = world.getTopology().wrapY(ycor);
    drawLine(this.xcor, this.ycor, xcor, ycor);

    this.xcor = newX;
    this.ycor = newY;

    variables[VAR_XCOR3D] = null;
    variables[VAR_YCOR3D] = null;
    currentPatch = null;
    Patch targetPatch = getPatchHere();
    if (originalPatch != targetPatch) {
      originalPatch.removeTurtle(this);
      targetPatch.addTurtle(this);
    }
    Observer observer = world.observer();
    if (this == observer.targetAgent()) {
      observer.updatePosition();
    }
    if (world.tieManager.hasTies()) {
      turtleMoved(xcor, ycor, zcor, oldX, oldY, zcor);
    }
  }

  @Override
  public void xandycor(Double xcor, Double ycor)
      throws AgentException {
    Patch originalPatch = getPatchHere();
    double oldX = this.xcor;
    double oldY = this.ycor;

    double x = xcor.doubleValue();
    double y = ycor.doubleValue();

    double wrappedX = world.getTopology().wrapX(x);
    double wrappedY = world.getTopology().wrapY(y);

    drawLine(this.xcor, this.ycor, x, y);

    this.xcor = wrappedX;
    this.ycor = wrappedY;
    variables[VAR_XCOR3D] = (x == wrappedX) ? xcor : null;
    variables[VAR_YCOR3D] = (y == wrappedY) ? ycor : null;
    currentPatch = null;
    Patch targetPatch = getPatchHere();
    if (originalPatch != targetPatch) {
      originalPatch.removeTurtle(this);
      targetPatch.addTurtle(this);
    }
    Observer observer = world.observer();
    if (this == observer.targetAgent()) {
      observer.updatePosition();
    }
    if (world.tieManager.hasTies()) {
      turtleMoved(x, y, zcor, oldX, oldY, zcor);
    }
  }

  public void xyandzcor(double xcor, double ycor, double zcor) {
    Patch originalPatch = getPatchHere();
    double oldX = this.xcor;
    double oldY = this.ycor;
    double oldZ = this.zcor;

    World3D w = (World3D) world;
    double newX = Topology.wrap(xcor, w.minPxcor() - 0.5, w.maxPxcor() + 0.5);
    double newY = Topology.wrap(ycor, w.minPycor() - 0.5, w.maxPycor() + 0.5);
    double newZ = Topology.wrap(zcor, w.minPzcor() - 0.5, w.maxPzcor() + 0.5);

    drawLine(this.xcor, this.ycor, this.zcor, xcor, ycor, zcor);

    this.xcor = newX;
    this.ycor = newY;
    this.zcor = newZ;

    variables[VAR_XCOR3D] = null;
    variables[VAR_YCOR3D] = null;
    variables[VAR_ZCOR3D] = null;

    currentPatch = null;
    Patch targetPatch = getPatchHere();
    if (originalPatch != targetPatch) {
      originalPatch.removeTurtle(this);
      targetPatch.addTurtle(this);
    }
    Observer observer = world.observer();
    if (this == observer.targetAgent()) {
      observer.updatePosition();
    }
    if (world.tieManager.hasTies()) {
      turtleMoved(xcor, ycor, zcor, oldX, oldY, oldZ);
    }
  }

  public void xyandzcor(Double xcor, Double ycor, Double zcor) {
    Patch originalPatch = getPatchHere();
    double oldX = this.xcor;
    double oldY = this.ycor;
    double oldZ = this.zcor;

    double x = xcor.doubleValue();
    double y = ycor.doubleValue();
    double z = zcor.doubleValue();
    World3D w = (World3D) world;
    double wrappedX = Topology.wrap(x, w.minPxcor() - 0.5, w.maxPxcor() + 0.5);
    double wrappedY = Topology.wrap(y, w.minPycor() - 0.5, w.maxPycor() + 0.5);
    double wrappedZ = Topology.wrap(z, w.minPzcor() - 0.5, w.maxPzcor() + 0.5);

    drawLine(this.xcor, this.ycor, this.zcor, x, y, z);

    this.xcor = wrappedX;
    this.ycor = wrappedY;
    this.zcor = wrappedZ;
    variables[VAR_XCOR3D] = (x == wrappedX) ? xcor : null;
    variables[VAR_YCOR3D] = (y == wrappedY) ? ycor : null;
    variables[VAR_ZCOR3D] = (z == wrappedZ) ? zcor : null;
    currentPatch = null;
    Patch targetPatch = getPatchHere();
    if (originalPatch != targetPatch) {
      originalPatch.removeTurtle(this);
      targetPatch.addTurtle(this);
    }
    Observer observer = world.observer();
    if (this == observer.targetAgent()) {
      observer.updatePosition();
    }
    if (world.tieManager.hasTies()) {
      turtleMoved(x, y, z, oldX, oldY, oldZ);
    }
  }

  @Override
  public void home() {
    xyandzcor(World.ZERO, World.ZERO, World.ZERO);
  }

  @Override
  public void face(Agent agent, boolean wrap) {
    double newHeading, newPitch;
    try {
      newHeading = world.protractor().towards(this, agent, wrap); // true = wrap
    } catch (AgentException ex) {
      newHeading = heading();
    }
    try {
      newPitch = world.protractor().towardsPitch(this, agent, wrap);
    } catch (AgentException ex) {
      newPitch = pitch();
    }
    headingPitchAndRoll(newHeading, newPitch, this.roll());
  }

  public void face(double x, double y, double z, boolean wrap) {
    double newHeading = heading();
    double newPitch = pitch();
    try {
      newHeading = world.protractor().towards(this, x, y, wrap);
    } catch (AgentException ex) {
      // AgentException here means we tried to calculate the heading from
      // an agent to itself, or to an agent at the exact same position.
      // Since face is nice, it just ignores the exception and doesn't change
      // the callers heading. - AZS 6/22/05
      org.nlogo.api.Exceptions.ignore(ex);
    }
    try {
      newPitch = world.protractor().towardsPitch(this, x, y, z, wrap);
    } catch (AgentException ex) {
      org.nlogo.api.Exceptions.ignore(ex);
    }

    headingPitchAndRoll(newHeading, newPitch, roll());
  }

  @Override
  public double dx() {
    return StrictMath.cos(StrictMath.toRadians(pitch())) *
        StrictMath.sin(StrictMath.toRadians(heading()));
  }

  @Override
  public double dy() {
    return StrictMath.cos(StrictMath.toRadians(pitch())) *
        StrictMath.cos(StrictMath.toRadians(heading()));
  }

  public double dz() {
    return StrictMath.sin(StrictMath.toRadians(pitch()));
  }

  @Override
  public String shape() {
    return (String) variables[VAR_SHAPE3D];
  }

  @Override
  public void shape(String shape) {
    variables[VAR_SHAPE3D] = shape;
  }

  @Override
  public Object label() {
    return variables[VAR_LABEL3D];
  }

  @Override
  public String labelString() {
    return Dump.logoObject(variables[VAR_LABEL3D]);
  }

  @Override
  public void label(Object label) {
    variables[VAR_LABEL3D] = label;
  }

  @Override
  public Object labelColor() {
    return variables[VAR_LABELCOLOR3D];
  }

  @Override
  public void labelColor(double labelColor) {
    variables[VAR_LABELCOLOR3D] = Double.valueOf(Color.modulateDouble(labelColor));
  }

  @Override
  public AgentSet getBreed() {
    return (AgentSet) variables[VAR_BREED3D];
  }

  /**
   * This version of setBreed properly resets the global breed AgentSets
   * Caller should ensure that the turtle isn't a link (links aren't
   * allowed to change breed).
   */
  @Override
  public void setBreed(AgentSet breed) {
    AgentSet oldBreed = null;
    if (variables[VAR_BREED3D] instanceof AgentSet) {
      oldBreed = (AgentSet) variables[VAR_BREED3D];
      if (breed == oldBreed) {
        return;
      }
      if (oldBreed != world.turtles()) {
        ((TreeAgentSet) variables[VAR_BREED3D]).remove(agentKey());
      }
    }
    if (breed != world.turtles()) {
      ((TreeAgentSet) breed).add(this);
    }
    variables[VAR_BREED3D] = breed;
    shape(world.turtleBreedShapes.breedShape(breed));
    realloc(false, oldBreed);
  }

  @Override
  public Patch getPatchAtHeadingAndDistance(double delta, double distance)
      throws AgentException {
    double[] angles = right(delta);
    return ((Protractor3D) world.protractor()).getPatchAtHeadingPitchAndDistance
        (xcor, ycor, zcor,
            angles[0], angles[1], distance);
  }

  @Override
  public void turnRight(double delta) {
    double[] angles = right(delta);

    headingPitchAndRoll(angles[0], angles[1], angles[2]);
  }

  public double[] right(double delta) {
    delta = -delta;
    Vect[] v = Vect.toVectors(heading, pitch(), roll());

    double sinDelta = StrictMath.sin(StrictMath.toRadians(delta));
    double cosDelta = StrictMath.cos(StrictMath.toRadians(delta));
    if (StrictMath.abs(sinDelta) < org.nlogo.api.Constants.Infinitesimal()) {
      sinDelta = 0;
    }
    if (StrictMath.abs(cosDelta) < org.nlogo.api.Constants.Infinitesimal()) {
      cosDelta = 0;
    }

    Vect turnForward = new Vect(-sinDelta, cosDelta, 0);

    Vect turnRight = new Vect(cosDelta, sinDelta, 0);

    Vect orthogonal = v[1].cross(v[0]);

    Vect forward =
        Vect.axisTransformation(turnForward, v[1], v[0], orthogonal);
    Vect right =
        Vect.axisTransformation(turnRight, v[1], v[0], orthogonal);

    return Vect.toAngles(forward, right);
  }

  @Override
  public boolean hidden() {
    return ((Boolean) variables[VAR_HIDDEN3D]).booleanValue();
  }

  @Override
  public void hidden(boolean hidden) {
    variables[VAR_HIDDEN3D] = hidden ? Boolean.TRUE : Boolean.FALSE;
  }

  @Override
  public double size() {
    return ((Double) variables[VAR_SIZE3D]).doubleValue();
  }

  @Override
  public void size(double size) {
    variables[VAR_SIZE3D] = Double.valueOf(size);
  }

  @Override
  public double penSize() {
    return ((Double) variables[VAR_PENSIZE3D]).doubleValue();
  }

  @Override
  public void penSize(double penSize) {
    variables[VAR_PENSIZE3D] = Double.valueOf(penSize);
  }

  @Override
  public String penMode() {
    return (String) variables[VAR_PENMODE3D];
  }

  @Override
  public void penMode(String penMode) {
    variables[VAR_PENMODE3D] = penMode;
  }

  ///

  private void turtleMoved(double newX, double newY, double newZ, double oldX, double oldY, double oldZ) {
    ((TieManager3D) world.tieManager).turtleMoved(this, newX, newY, newZ, oldX, oldY, oldZ);
  }

  private void turtleOrientationChanged(double newHeading, double newPitch, double newRoll,
                                        double oldHeading, double oldPitch, double oldRoll) {
    ((TieManager3D) world.tieManager).turtleOrientationChanged(this, newHeading, newPitch, newRoll,
        oldHeading, oldPitch, oldRoll);
  }

}
