// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.mirroring;

public strictfp class Agent {
  static final long serialVersionUID = 0L;
  final long id;
  final AgentType type;

  public Agent(long id, Class<? extends org.nlogo.api.Agent> agentClass) {
    this.id = id;
    type = AgentType.fromAgentClass(agentClass);
  }

  public Agent(java.io.DataInputStream is) {
    try {
      id = is.readLong();
      type = AgentType.fromInt(is.readInt());
    } catch (java.io.IOException e) {
      throw new IllegalStateException();
    }
  }

  void serialize(java.io.DataOutputStream os)
      throws java.io.IOException {
    os.writeLong(id);
    os.writeInt(type.toInt());
  }

  // must be called from the event thread
  public byte[] toByteArray() {
    java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
    try {
      serialize(new java.io.DataOutputStream(bos));
    } catch (java.io.IOException e) {
      // shouldn't happen, since we're writing to a byte array
      throw new IllegalStateException(e);
    }
    return bos.toByteArray();
  }

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
}
