// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.core.AgentKind

abstract class IndexedAgentSet(kind: AgentKind, printName: String) extends AgentSet(kind, printName) {
  def getByIndex(index: Int): Agent
}
