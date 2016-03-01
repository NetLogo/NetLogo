// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.mirroring;

import org.nlogo.core.AgentKind;
import org.nlogo.hubnet.mirroring.Agent.AgentType;

public strictfp class OverrideList
    implements java.io.Serializable {
  static final long serialVersionUID = 0L;

  public AgentType type;
  public int variable;

  public OverrideList(AgentKind agentKind, String varName) {
    type = AgentType.fromAgentKind(agentKind);
    variable = getOverrideIndex(type, varName);
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

  public static int getOverrideIndex(AgentType type, String varName) {
    switch (type) {
      case TURTLE:
        return TurtleData.getOverrideIndex(varName);
      case PATCH:
        return PatchData.getOverrideIndex(varName);
      case LINK:
        return LinkData.getOverrideIndex(varName);
      default:
        throw new IllegalStateException();
    }
  }

}
