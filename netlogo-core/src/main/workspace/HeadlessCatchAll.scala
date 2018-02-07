// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.core.{ AgentKind, WorldDimensions }
import org.nlogo.api.Logger
import org.nlogo.nvm.{ EditorWorkspace, LoggingWorkspace }

// This trait implements a bunch of methods that Headless doesn't care about.

trait HeadlessCatchAll
extends LoggingWorkspace
with Logger
with EditorWorkspace
with DefaultWorldLoader { this: AbstractWorkspace =>
  def silent: Boolean
  def startLogging(properties: String) = unsupported
  def deleteLogFiles() = unsupported
  def logCustomMessage(msg: String): Unit = unsupported
  def logCustomGlobals(nameValuePairs: (String, String)*): Unit = unsupported
  def zipLogFiles(filename: String) = unsupported
  def magicOpen(name: String) = unsupported
  override def inspectAgent(agent: org.nlogo.api.Agent, radius: Double) {
    if (!silent)
      println(agent)
  }
  def inspectAgent(agentClass: AgentKind, agent: org.nlogo.agent.Agent, radius: Double) {
    if (!silent) {
      println(agent)
    }
  }
  override def stopInspectingAgent(agent: org.nlogo.agent.Agent): Unit = { }
  override def stopInspectingDeadAgents(): Unit = { }
  override def calculateHeight(d: WorldDimensions) =
    (d.height * d.patchSize).toInt
  override def calculateWidth(d: WorldDimensions): Int =
    (d.width * d.patchSize).toInt
  override def computePatchSize(width: Int, numPatches: Int): Double =
    width / numPatches
  override def resizeView() { }
  override def updateDisplay(haveWorldLockAlready: Boolean, forced: Boolean) { }
  private def unsupported = throw new UnsupportedOperationException
}
