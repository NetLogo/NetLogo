// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api

// for extra-efficient agent class checking.  store as byte when space is paramount, store as int
// when speed is paramount.

object AgentBit {
  private val bits: Map[api.AgentKind, Int] =
    Map(api.AgentKind.Observer -> 1,
        api.AgentKind.Turtle -> 2,
        api.AgentKind.Patch -> 4,
        api.AgentKind.Link -> 8)
  def apply(kind: api.AgentKind): Int = bits(kind)
  def fromAgentClassString(s: String): Int =
    (if (s.indexOf('O') == -1) 0 else bits(api.AgentKind.Observer)) |
    (if (s.indexOf('T') == -1) 0 else bits(api.AgentKind.Turtle)) |
    (if (s.indexOf('P') == -1) 0 else bits(api.AgentKind.Patch)) |
    (if (s.indexOf('L') == -1) 0 else bits(api.AgentKind.Link))
}
