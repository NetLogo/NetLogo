// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

class LinkManager3D(world3D: World3D) extends LinkManagerImpl(world3D) {
  override def newLink(world: World, src: Turtle, dest: Turtle, breed: AgentSet): Link =
    new Link3D(world, src, dest, breed)
}
