// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.core.AgentKind
import org.nlogo.agent.Agent

trait MonitorManager {
  def inspect(agentKind: AgentKind, a0: Agent, radius: Double, workspace: GUIWorkspaceScala): Unit
  def stopInspecting(agent: Agent): Unit
  def stopInspectingDeadAgents(): Unit
  def closeAll()
}

// As of the time this was created, it is only used by LiteWorkspace
object NullMonitorManager extends MonitorManager {
  def inspect(agentKind: AgentKind, a0: Agent, radius: Double, workspace: GUIWorkspaceScala): Unit = { }
  def stopInspecting(agent: Agent): Unit = { }
  def stopInspectingDeadAgents(): Unit = { }
  def closeAll() = { }
}
