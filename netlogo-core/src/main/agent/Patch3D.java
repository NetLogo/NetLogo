// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

import org.nlogo.api.AgentException;
import org.nlogo.api.AgentVariableNumbers;
import org.nlogo.api.AgentVariables;
import org.nlogo.api.Color;
import org.nlogo.api.Dump;
import org.nlogo.core.LogoList;

import java.util.List;

public final strictfp class Patch3D
    extends Patch
    implements Agent3D, org.nlogo.api.Patch3D {
  public static final int VAR_PXCOR3D = AgentVariableNumbers.VAR_PXCOR3D;
  public static final int VAR_PYCOR3D = AgentVariableNumbers.VAR_PYCOR3D;
  public static final int VAR_PZCOR3D = AgentVariableNumbers.VAR_PZCOR3D;
  public static final int VAR_PCOLOR3D = AgentVariableNumbers.VAR_PCOLOR3D;
  public static final int VAR_PLABEL3D = AgentVariableNumbers.VAR_PLABEL3D;
  public static final int VAR_PLABELCOLOR3D = AgentVariableNumbers.VAR_PLABELCOLOR3D;

  public final int pzcor;

  public int pzcor() {
    return pzcor;
  }

  public AgentSet patchNeighbors6;

  @Override
  void topologyChanged() {
    super.topologyChanged();
    patchNeighbors6 = null;
  }

  public static final int LAST_PREDEFINED_VAR_3D = 5;

  Patch3D(World world, int id, int pxcor, int pycor, int pzcor, int numVariables) {
    super(world, pxcor, pycor);
    this.setId(id);
    this.pzcor = pzcor;

    Object[] variables = new Object[numVariables];

    for (int i = 0; i < numVariables; i++) {
      switch (i) {
        case VAR_PXCOR3D:
          variables[i] = Double.valueOf(pxcor);
          break;
        case VAR_PYCOR3D:
          variables[i] = Double.valueOf(pycor);
          break;
        case VAR_PZCOR3D:
          variables[i] = Double.valueOf(pzcor);
          break;
        case VAR_PCOLOR3D:
          variables[i] = Double.valueOf(World.Zero());
          break;
        case VAR_PLABEL3D:
          variables[i] = "";
          break;
        case VAR_PLABELCOLOR3D:
          variables[i] = Color.BoxedWhite();
          break;
        default:
          variables[i] = World.Zero();
          break;
      }
    }

    NUMBER_PREDEFINED_VARS = LAST_PREDEFINED_VAR_3D + 1;
    setVariables(variables);
  }

  @Override
  public void setPatchVariable(int vn, Object value)
      throws AgentException {
    if (vn > LAST_PREDEFINED_VAR_3D) {
      variables()[vn] = value;
    } else {
      switch (vn) {
        case VAR_PCOLOR3D:
          if (value instanceof Double) {
            pcolor((Double) value);
          } else if (value instanceof LogoList) {
            pcolor((LogoList) value, VAR_PCOLOR3D, true);
          } else {
            wrongTypeForVariable(AgentVariables.getImplicitPatchVariables(true)[vn],
                Double.class, value);
          }
          break;

        case VAR_PLABEL3D:
          label(value);
          break;

        case VAR_PLABELCOLOR3D:
          if (value instanceof Double) {
            labelColor(((Double) value).doubleValue());
          } else if (value instanceof LogoList) {
            labelColor((LogoList) value, VAR_PLABELCOLOR3D);
          } else {
            wrongTypeForVariable(AgentVariables.getImplicitPatchVariables(true)[vn],
                Double.class, value);
          }
          break;

        case VAR_PXCOR3D:
        case VAR_PYCOR3D:
        case VAR_PZCOR3D:
          throw new AgentException("you can't change a patch's coordinates");

        default:
          return;
      }
    }
  }

  @Override
  public void setPatchVariable(int vn, double value)
      throws AgentException {
    switch (vn) {
      case VAR_PXCOR3D:
        throw new AgentException("you can't change a patch's coordinates");
      case VAR_PYCOR3D:
        throw new AgentException("you can't change a patch's coordinates");
      case VAR_PZCOR3D:
        throw new AgentException("you can't change a patch's coordinates");
      default:
        throw new IllegalArgumentException
            (vn + " is not a double variable");
    }
  }

  @Override
  public Object getPatchVariable(int vn) {
    return variables()[vn];
  }

  @Override
  public double getPatchVariableDouble(int vn) {
    switch (vn) {
      case VAR_PXCOR3D:
        return pxcor;
      case VAR_PYCOR3D:
        return pycor;
      case VAR_PZCOR3D:
        return pzcor;
      default:
        throw new IllegalArgumentException
            (vn + " is not a double variable");
    }
  }

  @Override
  public Patch getPatchAtOffsets(double dx, double dy)
      throws AgentException {
    Patch target = ((World3D) _world).getPatchAt(pxcor + dx, pycor + dy, pzcor);
    if (target == null) {
      throw new AgentException("Cannot get patch beyond limits of current world.");
    }
    return target;
  }

  public Patch3D getPatchAtOffsets(double dx, double dy, double dz)
      throws AgentException {
    Patch3D target = ((World3D) _world).getPatchAt(pxcor + dx, pycor + dy, pzcor + dz);
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

  @Override
  public Patch fastGetPatchAt(int x, int y) {
    return ((World3D) _world).fastGetPatchAt(x, y, pzcor);
  }

  @Override
  public Turtle sprout(int c, int heading, AgentSet breed) {
    Turtle child = new Turtle3D((World3D) _world, breed,
        (Double) variables()[VAR_PXCOR3D],
        (Double) variables()[VAR_PYCOR3D],
        (Double) variables()[VAR_PZCOR3D]);
    child.colorDouble
        (Double.valueOf
            (5 + 10 * c));
    child.heading(heading);
    return child;
  }

  @Override
  public Object pcolor() {
    return _variables[VAR_PCOLOR3D];
  }

  @Override
  public void pcolor(double pcolor) {
    if (pcolor < 0 || pcolor >= Color.MaxColor()) {
      pcolor = Color.modulateDouble(pcolor);
    }
    Object currentColor = _variables[VAR_PCOLOR3D];
    if (!(currentColor instanceof Double) || ((Double) currentColor).doubleValue() != pcolor) {
      Double boxedColor = Double.valueOf(pcolor);
      _variables[VAR_PCOLOR3D] = boxedColor;
      _world.patchChangedColorAt((int) _id, pcolor);
    }
  }

  @Override
  public void pcolor(Double boxedColor) {
    double color = boxedColor.doubleValue();
    if (color < 0 || color >= Color.MaxColor()) {
      color = Color.modulateDouble(color);
      boxedColor = Double.valueOf(color);
    }
    Object currentColor = _variables[VAR_PCOLOR3D];
    if (!(currentColor instanceof Double) || ((Double) currentColor).doubleValue() != color) {
      _variables[VAR_PCOLOR3D] = boxedColor;
      _world.patchChangedColorAt((int) _id, color);
    }
  }

  @Override
  public void pcolor(LogoList rgb)
      throws AgentException {
    pcolor(rgb, VAR_PCOLOR3D, true);
  }

  @Override
  public void pcolorDoubleUnchecked(Double boxedColor) {
    double color = boxedColor.doubleValue();
    if (! (_variables[VAR_PCOLOR3D] instanceof Double) ||
         ((Double) _variables[VAR_PCOLOR3D]).doubleValue() != color) {
      _variables[VAR_PCOLOR3D] = boxedColor;
      _world.patchColors()[(int) _id] = Color.getARGBbyPremodulatedColorNumber(color);
      _world.markPatchColorsDirty();
      if (color != 0.0) {
        _world.patchesAllBlack(false);
      }
    }
  }

  @Override
  public Object label() {
    return _variables[VAR_PLABEL3D];
  }

  @Override
  public String labelString() {
    return Dump.logoObject(variables()[VAR_PLABEL3D]);
  }

  @Override
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
    variables()[VAR_PLABEL3D] = label;
  }

  @Override
  public Object labelColor() {
    return variables()[VAR_PLABELCOLOR3D];
  }

  @Override
  public void labelColor(double labelColor) {
    variables()[VAR_PLABELCOLOR3D] = Double.valueOf(Color.modulateDouble(labelColor));
  }

  @Override
  public void labelColor(Double labelColor) {
    variables()[VAR_PLABELCOLOR3D] = labelColor;
  }

  @Override
  public IndexedAgentSet getNeighbors() {
    if (patchNeighbors == null) {
      patchNeighbors = ((Topology3D) _world.topology()).getNeighbors3d(this);
    }
    return patchNeighbors;
  }

  public AgentSet getNeighbors6() {
    if (patchNeighbors6 == null) {
      patchNeighbors6 = ((Topology3D) _world.topology()).getNeighbors6(this);
    }
    return patchNeighbors6;
  }

  @Override
  public String toString() {
    return "patch " + pxcor + " " + pycor + " " + pzcor;
  }

  // special case black, non-RGB 3D patches to be invisible.  kinda janky to have a special case
  // like that but until we have an alpha variable I guess it's the least bad design. - ST 4/20/11
  @Override
  public int alpha() {
    return pcolor().equals(Color.BoxedBlack())
      ? 0
      : Color.getColor(pcolor()).getAlpha();
  }

}
