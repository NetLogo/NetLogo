// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api.AgentException

class Link3D(world: World3D, end1: Turtle, end2: Turtle, breed: AgentSet)
extends Link(world, end1, end2, breed)
with org.nlogo.api.Link3D {
  def z1 = end1.asInstanceOf[Turtle3D].zcor
  def z2 = world.topology.asInstanceOf[Topology3D]
    .shortestPathZ(end1.asInstanceOf[Turtle3D].zcor,
                   end2.asInstanceOf[Turtle3D].zcor)
  def pitch =
    try world.protractor.asInstanceOf[Protractor3D].towardsPitch(end1, end2, true)
    catch { case _: AgentException => 0 }
}
