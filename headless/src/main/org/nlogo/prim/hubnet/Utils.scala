// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.hubnet

import org.nlogo.{ api, agent }

object Utils {

  // because some HubNet code isn't up to speed with the new AgentKind stuff
  // yet. with a little work, this could (and should be eliminated) - ST 7/22/12, 3/22/13
  def kindToClass(kind: api.AgentKind): Class[_ <: agent.Agent] =
    kind match {
      case api.AgentKind.Observer =>
        classOf[agent.Observer]
      case api.AgentKind.Turtle =>
        classOf[agent.Turtle]
      case api.AgentKind.Patch =>
        classOf[agent.Patch]
      case api.AgentKind.Link =>
        classOf[agent.Link]
    }

}
