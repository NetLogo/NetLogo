// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.core

// for extra-efficient agent class checking.  store as byte when space is paramount, store as int
// when speed is paramount.

object AgentBit {
  private val bits: Map[core.AgentKind, Int] =
    Map(core.AgentKind.Observer -> 1,
        core.AgentKind.Turtle -> 2,
        core.AgentKind.Patch -> 4,
        core.AgentKind.Link -> 8)
  def apply(kind: core.AgentKind): Int = bits(kind)
  def fromAgentClassString(s: String): Int =
    (if (s.indexOf('O') == -1) 0 else bits(core.AgentKind.Observer)) |
    (if (s.indexOf('T') == -1) 0 else bits(core.AgentKind.Turtle)) |
    (if (s.indexOf('P') == -1) 0 else bits(core.AgentKind.Patch)) |
    (if (s.indexOf('L') == -1) 0 else bits(core.AgentKind.Link))
}
