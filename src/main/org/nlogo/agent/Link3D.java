package org.nlogo.agent;

import org.nlogo.api.AgentException;

public strictfp class Link3D
    extends Link
    implements org.nlogo.api.Link3D {
  public Link3D(World world, Turtle end1, Turtle end2, AgentSet breed) {
    super(world, end1, end2, breed);
  }

  public double z1() {
    return ((Turtle3D) end1).zcor();
  }

  public double z2() {
    return ((Topology3D) world.topology).shortestPathZ(((Turtle3D) end1).zcor(), ((Turtle3D) end2).zcor());
  }

  public double pitch() {
    try {
      return ((Protractor3D) world.protractor()).towardsPitch(end1, end2, true);
    } catch (AgentException e) {
      return 0;
    }
  }
}
