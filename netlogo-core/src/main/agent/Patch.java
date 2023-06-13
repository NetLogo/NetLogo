// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

import java.util.ArrayList;
import java.util.Arrays;

import org.nlogo.core.AgentKind;
import org.nlogo.core.AgentKindJ;
import org.nlogo.core.I18N;
import org.nlogo.core.LogoList;
import org.nlogo.core.Program;
import org.nlogo.api.AgentException;
import org.nlogo.api.AgentVariableNumbers;
import org.nlogo.api.AgentVariables;
import org.nlogo.api.Color;
import org.nlogo.api.Dump;
import org.nlogo.api.LogoException;

public class Patch
    extends Agent
    implements org.nlogo.api.Patch, AgentColors {

  public AgentKind kind() { return AgentKindJ.Patch(); }

  public static final int VAR_PXCOR = AgentVariableNumbers.VAR_PXCOR;
  public static final int VAR_PYCOR = AgentVariableNumbers.VAR_PYCOR;
  public static final int VAR_PCOLOR = AgentVariableNumbers.VAR_PCOLOR;
  public static final int VAR_PLABEL = AgentVariableNumbers.VAR_PLABEL;
  public static final int VAR_PLABELCOLOR = AgentVariableNumbers.VAR_PLABELCOLOR;

  public static final int LAST_PREDEFINED_VAR = 4;
  public int NUMBER_PREDEFINED_VARS = LAST_PREDEFINED_VAR + 1;

  // turtles here

  public Iterable<Turtle> turtlesHere() {
    return _turtlesHere;
  }

  private final ArrayList<Turtle> _turtlesHere = new ArrayList<Turtle>(0);

  public int turtleCount() {
    return _turtlesHere.size();
  }

  public void clearTurtles() {
    _turtlesHere.clear();
  }

  public void addTurtle(Turtle t) {
    _turtlesHere.add(t);
  }

  public void removeTurtle(Turtle t) {
    _turtlesHere.remove(t);
  }

  public AgentSet turtlesHereAgentSet() {
    return AgentSet.fromArray(AgentKindJ.Turtle(), _turtlesHere.toArray(new Turtle[_turtlesHere.size()]));
  }

  // 0 because user might never create any turtles!

  public IndexedAgentSet patchNeighbors;   // cached
  public IndexedAgentSet patchNeighbors4;  // cached

  void topologyChanged() {
    patchNeighbors = null;
    patchNeighbors4 = null;
  }

  Patch(World world, int id, int pxcor, int pycor, int numVariables) {
    super(world);
    this.setId(id);
    this.pxcor = pxcor;
    this.pycor = pycor;
    Object[] variables = new Object[numVariables];
    setVariables(variables);

    for (int i = 0; i < numVariables; i++) {
      switch (i) {
        case VAR_PXCOR:
          variables[i] = Double.valueOf(pxcor);
          break;
        case VAR_PYCOR:
          variables[i] = Double.valueOf(pycor);
          break;
        case VAR_PCOLOR:
          variables[i] = Double.valueOf(World.Zero());
          break;
        case VAR_PLABEL:
          variables[i] = "";
          break;
        case VAR_PLABELCOLOR:
          variables[i] = Color.BoxedWhite();
          break;
        default:
          variables[i] = World.Zero();
          break;
      }
    }
  }

  Patch(World world, int pxcor, int pycor) {
    super(world);
    this.pxcor = pxcor;
    this.pycor = pycor;
  }

  @Override
  public Agent realloc(Program oldProgram, Program newProgram) {
    boolean forRecompile = oldProgram != null;
    Object[] oldvars = _variables;
    Object[] newvars = new Object[_world.getVariablesArraySize(this)];
    for (int i = 0; newvars.length != i; i++) {
      if (i < NUMBER_PREDEFINED_VARS) {
        newvars[i] = oldvars[i];
      } else {
        newvars[i] = World.Zero();
      }
    }
    // Keep Variables Across Recompile
    if (forRecompile) {
      for (int i = NUMBER_PREDEFINED_VARS; i < oldvars.length && i < oldProgram.patchesOwn().size(); i++) {
        String name = oldProgram.patchesOwn().apply(i);
        int newpos = _world.patchesOwnIndexOf(name);
        if (newpos != -1) {
          newvars[newpos] = oldvars[i];
        }
      }
    }
    setVariables(newvars);

    return null;
  }

  @Override
  public Object getTurtleVariable(int vn)
      throws AgentException {
    throw new AgentException
        (I18N.errorsJ().get("org.nlogo.agent.Patch.cantAccessTurtleWithoutSpecifyingTurtle"));
  }


  @Override
  public Object getLinkVariable(int vn)
      throws AgentException {
    throw new AgentException
        (I18N.errorsJ().get("org.nlogo.agent.Patch.cantAccessLinkVarWithoutSpecifyingLink"));
  }

  @Override
  public Object getTurtleOrLinkVariable(String varName)
      throws AgentException {
    throw new AgentException
        (I18N.errorsJ().get("org.nlogo.agent.Patch.cantAccessTurtleOrLinkWithoutSpecifyingAgent"));
  }

  @Override
  public Object getLinkBreedVariable(String name)
      throws AgentException {
    throw new AgentException
        (I18N.errorsJ().get("org.nlogo.agent.Patch.cantAccessLinkVarWithoutSpecifyingLink"));
  }

  @Override
  public Object getBreedVariable(String name)
      throws AgentException {
    throw new AgentException(I18N.errorsJ().get("org.nlogo.agent.Patch.cantAccessTurtleWithoutSpecifyingTurtle"));
  }

  @Override
  public Object getVariable(int vn) {
    return getPatchVariable(vn);
  }

  public String variableName(int vn) {
    return _world.patchesOwnNameAt(vn);
  }

  @Override
  public void setTurtleVariable(int vn, Object value)
      throws AgentException {
    throw new AgentException(I18N.errorsJ().get("org.nlogo.agent.Patch.cantSetTurtleWithoutSpecifyingTurtle"));
  }

  @Override
  public void setTurtleVariable(int vn, double value)
      throws AgentException {
    throw new AgentException(I18N.errorsJ().get("org.nlogo.agent.Patch.cantSetTurtleWithoutSpecifyingTurtle"));
  }

  @Override
  public void setBreedVariable(String name, Object value)
      throws AgentException {
    throw new AgentException(I18N.errorsJ().get("org.nlogo.agent.Patch.cantSetTurtleWithoutSpecifyingTurtle"));
  }

  @Override
  public void setLinkVariable(int vn, Object value)
      throws AgentException {
    throw new AgentException
        (I18N.errorsJ().get("org.nlogo.agent.Patch.cantAccessLinkVarWithoutSpecifyingLink"));
  }

  @Override
  public void setLinkVariable(int vn, double value)
      throws AgentException {
    throw new AgentException
        (I18N.errorsJ().get("org.nlogo.agent.Patch.cantAccessLinkVarWithoutSpecifyingLink"));
  }

  @Override
  public void setTurtleOrLinkVariable(String varName, Object value)
      throws AgentException {
    throw new AgentException
        (I18N.errorsJ().get("org.nlogo.agent.Patch.cantAccessTurtleOrLinkWithoutSpecifyingAgent"));
  }

  @Override
  public void setLinkBreedVariable(String name, Object value)
      throws AgentException {
    throw new AgentException
        (I18N.errorsJ().get("org.nlogo.agent.Patch.cantAccessLinkVarWithoutSpecifyingLink"));
  }

  @Override
  public void setVariable(int vn, Object value)
      throws AgentException {
    setPatchVariable(vn, value);
  }

  void clearProgramVariables() {
    Arrays.fill(_variables, NUMBER_PREDEFINED_VARS, _variables.length, World.Zero());
  }

  @Override
  public void setPatchVariable(int vn, Object value)
      throws AgentException {
    if (vn > LAST_PREDEFINED_VAR) {
      _variables[vn] = value;
    } else {
      switch (vn) {
        case VAR_PCOLOR:
          if (value instanceof Double) {
            pcolor((Double) value);
          } else if (value instanceof LogoList) {
            pcolor((LogoList) value, VAR_PCOLOR, true);
          } else {
            wrongTypeForVariable(AgentVariables.getImplicitPatchVariables(false)[vn],
                Double.class, value);
          }
          break;

        case VAR_PLABEL:
          label(value);
          break;

        case VAR_PLABELCOLOR:
          if (value instanceof Double) {
            labelColor(((Double) value).doubleValue());
          } else if (value instanceof LogoList) {
            labelColor((LogoList) value, VAR_PLABELCOLOR);
          } else {
            wrongTypeForVariable(AgentVariables.getImplicitPatchVariables(false)[vn],
                Double.class, value);
          }
          break;

        case VAR_PXCOR:
        case VAR_PYCOR:
          throw new AgentException(I18N.errorsJ().get("org.nlogo.agent.Patch.cantChangePatchCoordinates"));

        default:
          throw new IllegalStateException(I18N.errorsJ().getN("org.nlogo.agent.Agent.cantSetUnknownVariable", vn));
      }
    }
    _world.notifyWatchers(this, vn, value);
  }

  @Override
  public void setPatchVariable(int vn, double value)
      throws AgentException {
    switch (vn) {
      case VAR_PXCOR:
        throw new AgentException(I18N.errorsJ().get("org.nlogo.agent.Patch.cantChangePatchCoordinates"));
      case VAR_PYCOR:
        throw new AgentException(I18N.errorsJ().get("org.nlogo.agent.Patch.cantChangePatchCoordinates"));
      default:
        throw new IllegalArgumentException(I18N.errorsJ().getN("org.nlogo.agent.Agent.notADoubleVariable", vn));
    }
  }

  @Override
  public Object getPatchVariable(int vn) {
    return _variables[vn];
  }

  public double getPatchVariableDouble(int vn) {
    switch (vn) {
      case VAR_PXCOR:
        return pxcor;
      case VAR_PYCOR:
        return pycor;
      default:
        throw new IllegalArgumentException(
            I18N.errorsJ().getN("org.nlogo.agent.Agent.notADoubleVariable", Integer.toString(vn)));
    }
  }

  @Override
  public Patch getPatchAtOffsets(double dx, double dy)
      throws AgentException {
    Patch target = _world.getPatchAt(pxcor + dx, pycor + dy);
    if (target == null) {
      throw new AgentException(I18N.errorsJ().get("org.nlogo.agent.Turtle.patchBeyondLimits"));
    }
    return target;
  }

  public Patch fastGetPatchAt(int x, int y) {
    return _world.fastGetPatchAt(x, y);
  }

  public IndexedAgentSet getNeighbors() {
    if (patchNeighbors == null) {
      patchNeighbors = _world.topology().getNeighbors(this);
    }
    return patchNeighbors;
  }

  public IndexedAgentSet getNeighbors4() {
    if (patchNeighbors4 == null) {
      patchNeighbors4 = _world.topology().getNeighbors4(this);
    }
    return patchNeighbors4;
  }

  ///

  public Turtle sprout(int c, int heading, AgentSet breed) {
    Turtle child = _world.sprout(this, breed);
    double color = 5 + 10 * c;
    child.colorDoubleUnchecked(Double.valueOf(color));
    child.heading(heading);
    return child;
  }

  public Object pcolor() {
    return _variables[VAR_PCOLOR];
  }

  public void pcolor(double pcolor) {
    if (pcolor < 0 || pcolor >= Color.MaxColor()) {
      pcolor = Color.modulateDouble(pcolor);
    }
    Object currentColor = _variables[VAR_PCOLOR];
    if (!(currentColor instanceof Double) || ((Double) currentColor).doubleValue() != pcolor) {
      Double boxedColor = Double.valueOf(pcolor);
      _variables[VAR_PCOLOR] = boxedColor;
      _world.patchChangedColorAt((int) _id, pcolor);
    }
  }

  public void pcolor(Double boxedColor) {
    double color = boxedColor.doubleValue();
    if (color < 0 || color >= Color.MaxColor()) {
      color = Color.modulateDouble(color);
      boxedColor = Double.valueOf(color);
    }
    Object currentColor = _variables[VAR_PCOLOR];
    if (!(currentColor instanceof Double) || ((Double) currentColor).doubleValue() != color) {
      _variables[VAR_PCOLOR] = boxedColor;
      _world.patchChangedColorAt((int) _id, color);
    }
  }

  public void pcolorDoubleUnchecked(Double boxedColor) {
    double color = boxedColor.doubleValue();
    Object currentColor = _variables[VAR_PCOLOR];
    if (!(currentColor instanceof Double) || ((Double) currentColor).doubleValue() != color) {
      _variables[VAR_PCOLOR] = boxedColor;
      _world.patchChangedColorAt((int) _id, color);
    }
  }

  public void pcolor(LogoList rgb)
      throws AgentException {
    pcolor(rgb, VAR_PCOLOR, true);
  }

  public void pcolor(LogoList rgb, int varIndex, boolean allowAlpha)
      throws AgentException {
    validRGBList(rgb, allowAlpha);

    if (!(_variables[varIndex] instanceof LogoList) || !rgb.equals(_variables[varIndex])) {
      _variables[varIndex] = rgb;
      int red   = ((Double) rgb.get(0)).intValue();
      int green = ((Double) rgb.get(1)).intValue();
      int blue  = ((Double) rgb.get(2)).intValue();
      _world.patchColors()[(int) _id] = Color.getRGBInt(red, green, blue);
      _world.markPatchColorsDirty();
      if (!((red   % 10 == 0) &&
            (green % 10 == 0) &&
            (blue  % 10 == 0))) {
        _world.patchesAllBlack(false);
      }
      if(rgb.size() > 3) {
        _world.mayHavePartiallyTransparentObjects(true);
      }
    }
  }

  ///

  public final int pxcor;

  public int pxcor() {
    return pxcor;
  }

  public final int pycor;

  public int pycor() {
    return pycor;
  }

  public Object label() {
    return _variables[VAR_PLABEL];
  }

  public boolean hasLabel() {
    return !(label() instanceof String &&
        ((String) label()).length() == 0);
  }

  public String labelString() {
    return Dump.logoObject(_variables[VAR_PLABEL]);
  }

  public void label(Object label) {
    if (label instanceof String &&
        ((String) label).length() == 0) {
      if (hasLabel()) {
        _world.removePatchLabel();
      }
    } else {
      if (!hasLabel()) {
        _world.addPatchLabel();
      }
    }
    _variables[VAR_PLABEL] = label;
  }

  public Object labelColor() {
    return _variables[VAR_PLABELCOLOR];
  }

  public void labelColor(double labelColor) {
    _variables[VAR_PLABELCOLOR] = Double.valueOf(Color.modulateDouble(labelColor));
  }

  public void labelColor(Double labelColor) {
    _variables[VAR_PLABELCOLOR] = labelColor;
  }

  public void labelColor(LogoList rgb, int varIndex)
      throws AgentException {
    validRGBList(rgb, true);
    _variables[varIndex] = rgb;
  }

  @Override
  public String toString() {
    return "patch " + pxcor + " " + pycor;
  }

  @Override
  public String classDisplayName() {
    return "patch";
  }

  public static final int BIT = 4;

  @Override
  public int agentBit() {
    return BIT;
  }

  public String shape() {
    return "";
  }

  public double size() {
    return 1;
  }

  /// getPatch<DIRECTION> methods -- we pass these off to the topology's methods
  public Patch getPatchNorth() {
    return _world.topology().getPN(this);
  }

  public Patch getPatchSouth() {
    return _world.topology().getPS(this);
  }

  public Patch getPatchEast() {
    return _world.topology().getPE(this);
  }

  public Patch getPatchWest() {
    return _world.topology().getPW(this);
  }

  public Patch getPatchNorthWest() {
    return _world.topology().getPNW(this);
  }

  public Patch getPatchSouthWest() {
    return _world.topology().getPSW(this);
  }

  public Patch getPatchSouthEast() {
    return _world.topology().getPSE(this);
  }

  public Patch getPatchNorthEast() {
    return _world.topology().getPNE(this);
  }

  public int alpha() {
    return org.nlogo.api.Color.getColor(pcolor()).getAlpha();
  }

}
