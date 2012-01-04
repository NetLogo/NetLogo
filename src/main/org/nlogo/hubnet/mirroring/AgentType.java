// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.mirroring;

public enum AgentType {
  TURTLE, PATCH, LINK, OBSERVER;

  public int toInt() {
    switch (this) {
      case TURTLE:
        return 0;
      case PATCH:
        return 1;
      case LINK:
        return 2;
      case OBSERVER:
        return 3;
      default:
        throw new IllegalStateException();
    }
  }

  public static AgentType fromAgentClass(Class<? extends org.nlogo.api.Agent> agentClass) {
    if (agentClass == null) {
      return OBSERVER;
    } // don't know why this seems to be necessary - ST 2/25/09
    if (org.nlogo.api.Turtle.class.isAssignableFrom(agentClass)) {
      return TURTLE;
    } else if (org.nlogo.api.Patch.class.isAssignableFrom(agentClass)) {
      return PATCH;
    } else if (org.nlogo.api.Link.class.isAssignableFrom(agentClass)) {
      return LINK;
    } else {
      return OBSERVER;
    }
  }

  public static AgentType fromInt(int type) {
    switch (type) {
      case 0:
        return TURTLE;
      case 1:
        return PATCH;
      case 2:
        return LINK;
      case 3:
        return OBSERVER;
      default:
        throw new IllegalStateException();
    }
  }

}
