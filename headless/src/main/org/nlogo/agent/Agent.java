// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent;

import org.nlogo.api.AgentException;
import org.nlogo.api.Dump;
import org.nlogo.api.I18N;
import org.nlogo.api.LogoList;

public abstract strictfp class Agent
    implements org.nlogo.api.Agent, Comparable<Agent> {

  final World world;

  public World world() {
    return world;
  }

  long id = 0;

  public long id() {
    return id;
  }

  Object[] variables = null;
  public Object[] variables() { return variables; }

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

  abstract void realloc(boolean forRecompile);

  public abstract Object getVariable(int vn);

  public abstract void setVariable(int vn, Object value)
      throws AgentException;

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
      throws AgentException;

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

  public abstract String classDisplayName();

  public abstract int agentBit();

}
