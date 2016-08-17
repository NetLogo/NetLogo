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

import java.util.ArrayList;

public strictfp class Patch
    extends Agent
    implements org.nlogo.api.Patch {

  public AgentKind kind() { return AgentKindJ.Patch(); }

  public String shape() { return ""; }

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
   return AgentSet.fromArray(
     AgentKindJ.Turtle(),
     _turtlesHere.toArray(new Agent[_turtlesHere.size()]));
  }

  // 0 because user might never create any turtles!

  public AgentSet patchNeighbors;   // cached
  public AgentSet patchNeighbors4;  // cached

  void topologyChanged() {
    patchNeighbors = null;
    patchNeighbors4 = null;
  }

  Patch(World world, int id, int pxcor, int pycor, int numVariables) {
    super(world);
    _id_$eq(id);
    this.pxcor = pxcor;
    this.pycor = pycor;
    _variables_$eq(new Object[numVariables]);

    for (int i = 0; i < numVariables; i++) {
      switch (i) {
        case VAR_PXCOR:
          variables()[i] = Double.valueOf(pxcor);
          break;
        case VAR_PYCOR:
          variables()[i] = Double.valueOf(pycor);
          break;
        case VAR_PLABEL:
          variables()[i] = "";
          break;
        case VAR_PLABELCOLOR:
          variables()[i] = Color.BoxedWhite();
          break;
        default:
          variables()[i] = World.ZERO;
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
  public void realloc(boolean forRecompile) {
    Object[] oldvars = variables();
    Object[] newvars = new Object[world().program().patchesOwn().size()];
    for (int i = 0; newvars.length != i; i++) {
      if (i < NUMBER_PREDEFINED_VARS) {
        newvars[i] = oldvars[i];
      } else {
        newvars[i] = World.ZERO;
      }
    }
    // Keep Variables Across Recompile
    if (forRecompile) {
      for (int i = NUMBER_PREDEFINED_VARS; i < oldvars.length && i < world().oldProgram.patchesOwn().size(); i++) {
        String name = world().oldProgram.patchesOwn().apply(i);
        int newpos = world().patchesOwnIndexOf(name);
        if (newpos != -1) {
          newvars[newpos] = oldvars[i];
        }
      }
    }
    _variables_$eq(newvars);
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
    return world().patchesOwnNameAt(vn);
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

  @Override
  public void setPatchVariable(int vn, Object value)
      throws AgentException {
    if (vn > LAST_PREDEFINED_VAR) {
      variables()[vn] = value;
    } else {
      switch (vn) {
        case VAR_PCOLOR:
          if (value instanceof Double) {
            pcolor((Double) value);
          } else if (value instanceof LogoList) {
            pcolor((LogoList) value, VAR_PCOLOR, false);
          } else {
            wrongTypeForVariable(AgentVariables.getImplicitPatchVariables()[vn],
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
            wrongTypeForVariable(AgentVariables.getImplicitPatchVariables()[vn],
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
    world().notifyWatchers(this, vn, value);
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
    if (vn == VAR_PCOLOR &&
        variables()[VAR_PCOLOR] == null) {
      variables()[VAR_PCOLOR] = Double.valueOf(pcolor);
    }
    return variables()[vn];
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
    Patch target = world().getPatchAt(pxcor + dx, pycor + dy);
    if (target == null) {
      throw new AgentException(I18N.errorsJ().get("org.nlogo.agent.Turtle.patchBeyondLimits"));
    }
    return target;
  }

  public AgentSet getNeighbors() {
    if (patchNeighbors == null) {
      patchNeighbors = world().topology().getNeighbors(this);
    }
    return patchNeighbors;
  }

  public AgentSet getNeighbors4() {
    if (patchNeighbors4 == null) {
      patchNeighbors4 = world().topology().getNeighbors4(this);
    }
    return patchNeighbors4;
  }

  ///

  public Turtle sprout(int c, int heading, AgentSet breed) {
    Turtle child = new Turtle(world(), breed,
        (Double) variables()[VAR_PXCOR],
        (Double) variables()[VAR_PYCOR]);
    double color = 5 + 10 * c;
    child.colorDoubleUnchecked(Double.valueOf(color));
    child.heading(heading);
    return child;
  }

  protected double pcolor = 0;

  public double pcolorDouble() {
    if (variables()[VAR_PCOLOR] == null || variables()[VAR_PCOLOR] instanceof Double) {
      return pcolor;
    }
    throw new IllegalStateException(I18N.errorsJ().get("org.nlogo.agent.Patch.pcolorNotADouble"));
  }

  public Object pcolor() {
    if (variables()[VAR_PCOLOR] == null) {
      variables()[VAR_PCOLOR] = Double.valueOf(pcolor);
    }
    return variables()[VAR_PCOLOR];
  }

  public void pcolor(double pcolor) {
    if (pcolor < 0 || pcolor >= Color.MaxColor()) {
      pcolor = Color.modulateDouble(pcolor);
    }
    if (this.pcolor != pcolor) {
      this.pcolor = pcolor;
      variables()[VAR_PCOLOR] = null;
      world().patchColors[(int) id()] = Color.getARGBbyPremodulatedColorNumber(pcolor);
      if (pcolor != 0.0) {
        world().patchesAllBlack = false;
      }
    }
  }

  public void pcolor(Double boxedColor) {
    double color = boxedColor.doubleValue();
    if (color < 0 || color >= Color.MaxColor()) {
      color = Color.modulateDouble(color);
      if (pcolor != color) {
        pcolor = color;
        variables()[VAR_PCOLOR] = null;
        world().patchColors[(int) id()] = Color.getARGBbyPremodulatedColorNumber(pcolor);
        if (pcolor != 0.0) {
          world().patchesAllBlack = false;
        }
      }
    } else if (pcolor != color) {
      pcolor = color;
      variables()[VAR_PCOLOR] = boxedColor;
      world().patchColors[(int) id()] = Color.getARGBbyPremodulatedColorNumber(pcolor);
      if (pcolor != 0.0) {
        world().patchesAllBlack = false;
      }
    }
  }

  public void pcolorDoubleUnchecked(Double boxedColor) {
    double color = boxedColor.doubleValue();
    if (color != pcolor) {
      pcolor = color;
      variables()[VAR_PCOLOR] = boxedColor;
      world().patchColors[(int) id()] = Color.getARGBbyPremodulatedColorNumber(color);
      if (color != 0.0) {
        world().patchesAllBlack = false;
      }
    }
  }

  public void pcolor(LogoList rgb)
      throws AgentException {
    pcolor(rgb, VAR_PCOLOR, true);
  }

  public void pcolor(LogoList rgb, int varIndex, boolean allowAlpha)
      throws AgentException {
    org.nlogo.api.Color.validRGBList(rgb, allowAlpha);
    pcolor = Double.NaN;

    if (!(variables()[varIndex] instanceof LogoList) || !rgb.equals(variables()[varIndex])) {
      variables()[varIndex] = rgb;
      world().patchColors[(int) id()] = Color.getRGBInt(((Double) rgb.get(0)).intValue(),
          ((Double) rgb.get(1)).intValue(),
          ((Double) rgb.get(2)).intValue());
      world().patchesAllBlack = false;
      if(rgb.size() > 3) {
        world().mayHavePartiallyTransparentObjects = true;
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
    return variables()[VAR_PLABEL];
  }

  public boolean hasLabel() {
    return !(label() instanceof String &&
        ((String) label()).length() == 0);
  }

  public String labelString() {
    return Dump.logoObject(variables()[VAR_PLABEL]);
  }

  public void label(Object label) {
    if (label instanceof String &&
        ((String) label).length() == 0) {
      if (hasLabel()) {
        world().patchesWithLabels--;
      }
    } else {
      if (!hasLabel()) {
        world().patchesWithLabels++;
      }
    }
    variables()[VAR_PLABEL] = label;
  }

  public Object labelColor() {
    return variables()[VAR_PLABELCOLOR];
  }

  public void labelColor(double labelColor) {
    variables()[VAR_PLABELCOLOR] = Double.valueOf(Color.modulateDouble(labelColor));
  }

  public void labelColor(Double labelColor) {
    variables()[VAR_PLABELCOLOR] = labelColor;
  }

  public void labelColor(LogoList rgb, int varIndex)
      throws AgentException {
    org.nlogo.api.Color.validRGBList(rgb, true);
    variables()[varIndex] = rgb;
  }

  @Override
  public String toString() {
    return "patch " + pxcor + " " + pycor;
  }

  @Override
  public String classDisplayName() {
    return "patch";
  }

  public static final int BIT = AgentBit.apply(AgentKindJ.Patch());

  @Override
  public int agentBit() {
    return BIT;
  }

  public double size() {
    return 1;
  }

  public int alpha() {
    return org.nlogo.api.Color.getColor(pcolor()).getAlpha();
  }

}
