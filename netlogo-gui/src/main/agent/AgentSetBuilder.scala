// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.core

import java.util.ArrayList

class AgentSetBuilder(kind: core.AgentKind, capacity: Int) {
  // for convenience from Java
  def this(kind: core.AgentKind) =
    this(kind, 1)
  private[this] var buf =
    new ArrayList[Agent](capacity)
  def add(agent: Agent) {
    buf.add(agent)
  }
  def contains(agent: Agent) =
    buf.contains(agent)
  def build(): IndexedAgentSet = {
    val agentArray = new Array[Agent](buf.size)
    buf.toArray(agentArray)
    val result = AgentSet.fromArray(kind, agentArray)
    buf = null // prevent repeated builds
    result
  }
}
