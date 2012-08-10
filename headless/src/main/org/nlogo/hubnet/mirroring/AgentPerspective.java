// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.mirroring;

import org.nlogo.api.Perspective;

public strictfp class AgentPerspective {
  static final long serialVersionUID = 0L;

  final Agent agent;
  final int perspective;
  final double radius;
  final boolean serverMode;

  public AgentPerspective(Class<? extends org.nlogo.api.Agent> agentClass, long id, int perspective, double radius, boolean serverMode) {
    this.agent = new Agent(id, agentClass);
    this.perspective = perspective;
    this.radius = radius;
    this.serverMode = serverMode;
  }

  public AgentPerspective(org.nlogo.api.Agent a, Perspective p,
                          double radius, boolean serverMode) {
    if (a == null) {
      agent = new Agent(0, org.nlogo.api.Observer.class);
    } else {
      agent = new Agent(a.id(), a.getClass());
    }
    perspective = p.export();
    this.radius = radius;
    this.serverMode = serverMode;
  }

  public AgentPerspective(java.io.DataInputStream is) {
    try {
      agent = new Agent(is);
      perspective = is.readInt();
      radius = is.readDouble();
      serverMode = is.readBoolean();
    } catch (java.io.IOException e) {
      throw new IllegalStateException();
    }
  }

  void serialize(java.io.DataOutputStream os)
      throws java.io.IOException {
    agent.serialize(os);
    os.writeInt(perspective);
    os.writeDouble(radius);
    os.writeBoolean(serverMode);
  }

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

  public boolean equals(org.nlogo.api.Agent a, Perspective p) {
    return a == null ? (agent.type == Agent.AgentType.OBSERVER && agent.id == 0 &&
        perspective == 0 && radius == -1) :
        ((agent.id == a.id())
            && (agent.type == Agent.AgentType.fromAgentClass(a.getClass()))
            && (p.export() == perspective) && radius == -1);
  }
}
