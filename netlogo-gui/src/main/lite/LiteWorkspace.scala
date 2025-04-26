// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lite

import java.awt.Frame

import org.nlogo.core.{ AgentKind, Model, Femto }
import org.nlogo.agent.{ Agent, World }
import org.nlogo.api.{ AggregateManagerInterface, ControlSet, NetLogoThreeDDialect, NetLogoLegacyDialect, RendererInterface, Version }
import org.nlogo.nvm.PresentationCompilerInterface
import org.nlogo.window.{ ErrorDialogManager, GUIWorkspace, NetLogoListenerManager, UpdateManager }
import org.nlogo.workspace.BufferedReaderImporter

class LiteWorkspace(LitePanel: LitePanel, world: World, frame: Frame, listenerManager: NetLogoListenerManager, errorDialogManager: ErrorDialogManager, controlSet: ControlSet)
extends GUIWorkspace(world, GUIWorkspace.KioskLevel.MODERATE, frame, frame, null, null, listenerManager, errorDialogManager, controlSet) {
  val compiler = Femto.get[PresentationCompilerInterface]("org.nlogo.compile.Compiler", if (Version.is3D) NetLogoThreeDDialect else NetLogoLegacyDialect)
  // lazy to avoid initialization order snafu - ST 3/1/11
  lazy val updateManager = new UpdateManager() {
    override def defaultFrameRate = LiteWorkspace.this.frameRate
    override def updateMode = LiteWorkspace.this.updateMode
    override def ticks = world.tickCounter.ticks
  }
  val aggregateManager =
    Femto.get[AggregateManagerInterface]("org.nlogo.sdm.AggregateManagerLite")
  override def doImport(importer: BufferedReaderImporter): Unit = {
    super.doImport(importer)
  }
  override def inspectAgent(agent: org.nlogo.api.Agent, radius: Double): Unit = { }
  override def inspectAgent(agentClass: AgentKind, agent: Agent, radius: Double): Unit = { }
  override def stopInspectingAgent(agent: org.nlogo.agent.Agent): Unit = { }
  override def stopInspectingDeadAgents(): Unit = { }
  override def closeAgentMonitors(): Unit = { }
  override def newRenderer = Femto.get[RendererInterface]("org.nlogo.render.Renderer", world)
  override def updateModel(m: Model): Model = m
}
