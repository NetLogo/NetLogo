package org.nlogo.agent;

public strictfp class LinkManager3D
    extends LinkManager {

  public LinkManager3D(World3D world3D) {
    super(world3D);
  }

  @Override
  Link newLink(World world, Turtle src, Turtle dest, AgentSet breed) {
    return new Link3D(world, src, dest, breed);
  }
}
