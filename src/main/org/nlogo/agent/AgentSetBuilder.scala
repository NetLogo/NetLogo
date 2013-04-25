// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api

class AgentSetBuilder(kind: api.AgentKind, capacity: Int) {
  // for convenience from Java
  def this(kind: api.AgentKind) =
    this(kind, 1)
  private[this] var buf =
    collection.mutable.ArrayBuffer[Agent]()
  def add(agent: Agent) {
    buf += agent
  }
  def contains(agent: Agent) =
    buf.contains(agent)
  def build(): AgentSet = {
    val result = AgentSet.fromArray(kind, buf.toArray)
    buf = null // prevent repeated builds
    result
  }
}
