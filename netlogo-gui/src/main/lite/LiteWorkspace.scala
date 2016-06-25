// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lite

import org.nlogo.core.{AgentKind, Femto, Model}
import org.nlogo.agent.{Agent, World}
import org.nlogo.api.{AggregateManagerInterface, FileIO, NetLogoLegacyDialect, NetLogoThreeDDialect, RendererInterface, Version}
import org.nlogo.nvm.CompilerInterface
import org.nlogo.window.{GUIWorkspace, NetLogoListenerManager, UpdateManager}
import org.nlogo.workspace.BufferedReaderImporter

class LiteWorkspace(appletPanel: AppletPanel, isApplet: Boolean, world: World, frame: java.awt.Frame, listenerManager: NetLogoListenerManager)
extends GUIWorkspace(world, GUIWorkspace.KioskLevel.MODERATE, frame, frame, null, null, listenerManager) {
  val compiler = Femto.get[CompilerInterface]("org.nlogo.compiler.Compiler", if (Version.is3D) NetLogoThreeDDialect else NetLogoLegacyDialect)
  // lazy to avoid initialization order snafu - ST 3/1/11
  lazy val updateManager = new UpdateManager() {
    override def defaultFrameRate = LiteWorkspace.this.frameRate
    override def updateMode = LiteWorkspace.this.updateMode
    override def ticks = world.tickCounter.ticks
  }
  val aggregateManager =
    Femto.get[AggregateManagerInterface]("org.nlogo.sdm.AggregateManagerLite")
  override def doImport(importer: BufferedReaderImporter) {
    if(isApplet)
      // it's pretty gruesome here efficiency-wise that we slurp
      // the entire contents into a giant string -- ST 9/29/04
      importer.doImport(
        new java.io.BufferedReader(
          new java.io.StringReader(
            FileIO.url2String(
              appletPanel.getFileURL(importer.filename).toString))))
    else
      super.doImport(importer)
  }
  override def inspectAgent(agent: org.nlogo.api.Agent, radius: Double) { }
  override def inspectAgent(agentClass: AgentKind, agent: Agent, radius: Double) { }
  override def stopInspectingAgent(agent: org.nlogo.agent.Agent): Unit = { }
  override def stopInspectingDeadAgents(): Unit = { }
  override def closeAgentMonitors() { }
  override def newRenderer = Femto.get[RendererInterface]("org.nlogo.render.Renderer", world)
  override def updateModel(m: Model): Model = m
}
