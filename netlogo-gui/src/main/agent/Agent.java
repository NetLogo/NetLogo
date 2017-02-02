// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

import org.nlogo.core.AgentKind;
import org.nlogo.core.I18N;
import org.nlogo.core.LogoList;
import org.nlogo.api.AgentException;
import org.nlogo.api.Dump;
import org.nlogo.api.LogoException;
import org.nlogo.api.ValueConstraint;

import java.util.Observable;

public abstract strictfp class Agent
    extends Observable
    implements org.nlogo.api.Agent, Comparable<Agent> {

  final World world;

  public World world() {
    return world;
  }

  public long id = 0;

  public long id() {
    return id;
  }

  public Object[] variables() { return variables; }
  public Object[] variables = null; // public ONLY for __fire
  ValueConstraint[] variableConstraints = null;

  Object agentKey() {
    return Double.valueOf(id);
  }

  public Agent(World world) {
    this.world = world;
  }

  // implement Comparable
  public int compareTo(Agent a) {
    long otherId = a.id;
    return id < otherId
        ? -1
        : (id > otherId ? 1 : 0);
  }

  abstract Agent realloc(boolean forRecompile)
      throws AgentException;

  public int getVariableCount() {
    return variables.length;
  }

  public abstract Object getVariable(int vn);

  /**
   * Returns the name of the variable with the given index. Works for built-in, *-own, and breed variables.
   * Only added in NetLogo 5.0.6.
   * @param vn the index of the variable
   */
  public abstract String variableName(int vn);

  public abstract void setVariable(int vn, Object value)
      throws AgentException, LogoException;

  public ValueConstraint variableConstraint(int vn) {
    return variableConstraints[vn];
  }

  public void variableConstraint(int vn, ValueConstraint con) {
    variableConstraints[vn] = con;
  }

  public abstract Object getObserverVariable(int vn);

  public abstract Object getTurtleVariable(int vn)
      throws AgentException;

  public abstract Object getBreedVariable(String name)
      throws AgentException;

  public abstract Object getLinkBreedVariable(String name)
      throws AgentException;

  public abstract Object getLinkVariable(int vn)
      throws AgentException;

  public abstract Object getPatchVariable(int vn)
      throws AgentException;

  public abstract Object getTurtleOrLinkVariable(String varName)
      throws AgentException;

  public abstract void setObserverVariable(int vn, Object value)
      throws AgentException, LogoException;

  public abstract void setTurtleVariable(int vn, Object value)
      throws AgentException;

  public abstract void setTurtleVariable(int vn, double value)
      throws AgentException;

  public abstract void setLinkVariable(int vn, Object value)
      throws AgentException;

  public abstract void setLinkVariable(int vn, double value)
      throws AgentException;

  public abstract void setBreedVariable(String name, Object value)
      throws AgentException;

  public abstract void setLinkBreedVariable(String name, Object value)
      throws AgentException;

  public abstract void setPatchVariable(int vn, Object value)
      throws AgentException;

  public abstract void setPatchVariable(int vn, double value)
      throws AgentException;

  public abstract void setTurtleOrLinkVariable(String varName, Object value)
      throws AgentException;

  public abstract Patch getPatchAtOffsets(double dx, double dy) throws AgentException;

  void wrongTypeForVariable(String name, Class<?> expectedClass, Object value)
      throws AgentException {
    throw new AgentException(I18N.errorsJ().getN("org.nlogo.agent.Agent.wrongTypeOnSetError",
        classDisplayName(), name, Dump.typeName(expectedClass), Dump.logoObject(value)));
  }

  void validRGBList(LogoList rgb, boolean allowAlpha)
      throws AgentException {
    if (rgb.size() == 3 || (allowAlpha && rgb.size() == 4)) {
      try {
        for (int i = 0; i < rgb.size(); i++) {
          validRGB(((Double) rgb.get(i)).intValue());
        }
        return;
      } catch (ClassCastException e) {
        // just fall through and throw the error below
        org.nlogo.api.Exceptions.ignore(e);
      }
    }
    String key =
        allowAlpha
            ? "org.nlogo.agent.Agent.rgbListSizeError.3or4"
            : "org.nlogo.agent.Agent.rgbListSizeError.3";
    throw new AgentException(I18N.errorsJ().get(key));
  }

  private void validRGB(int c)
      throws AgentException {
    if (c < 0 || c > 255) {
      throw new AgentException(I18N.errorsJ().get("org.nlogo.agent.Agent.rgbValueError"));
    }
  }

  public abstract String classDisplayName();

  public abstract Class<? extends Agent> getAgentClass();

  public abstract int getAgentBit();

  public int agentBit() {
    return getAgentBit();
  }

  public boolean isPartiallyTransparent() {
    int alpha = alpha();
    return alpha > 0 && alpha < 255;
  }

}

