// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.mirroring;

import org.nlogo.hubnet.mirroring.AgentType;

public strictfp class OverrideList
    implements java.io.Serializable {

  public AgentType type;
  public int variable;

  public OverrideList(Class<? extends org.nlogo.api.Agent> agentClass, String varName) {
    type = AgentType.fromAgentClass(agentClass);
    variable = Overridable.getOverrideIndex(type, varName);
  }

  private void readObject(java.io.ObjectInputStream in)
      throws java.io.IOException {
    type = AgentType.fromInt(in.readInt());
    variable = in.readInt();
  }

  private void writeObject(java.io.ObjectOutputStream out)
      throws java.io.IOException {
    out.writeInt(type.toInt());
    out.writeInt(variable);
  }

}
