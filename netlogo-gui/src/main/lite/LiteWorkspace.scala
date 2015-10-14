// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lite

import org.nlogo.agent.{ Agent, World }
import org.nlogo.api.{ AggregateManagerInterface, RendererInterface }
import org.nlogo.nvm.CompilerInterface
import org.nlogo.util.Femto
import org.nlogo.window.{ GUIWorkspace, NetLogoListenerManager, UpdateManager }
import org.nlogo.workspace.BufferedReaderImporter

class LiteWorkspace(appletPanel: AppletPanel, isApplet: Boolean, world: World, frame: java.awt.Frame, listenerManager: NetLogoListenerManager)
extends GUIWorkspace(world, GUIWorkspace.KioskLevel.MODERATE, frame, frame, null, null, listenerManager) {
  val compiler = Femto.scalaSingleton(
    classOf[CompilerInterface], "org.nlogo.compiler.Compiler")
  // lazy to avoid initialization order snafu - ST 3/1/11
  lazy val updateManager = new UpdateManager() {
    override def defaultFrameRate = LiteWorkspace.this.frameRate
    override def updateMode = LiteWorkspace.this.updateMode
    override def ticks = world.tickCounter.ticks
  }
  val aggregateManager =
    Femto.get(classOf[AggregateManagerInterface],
              "org.nlogo.sdm.AggregateManagerLite", Array())
  override def doImport(importer: BufferedReaderImporter) {
    if(isApplet)
      // it's pretty gruesome here efficiency-wise that we slurp
      // the entire contents into a giant string -- ST 9/29/04
      importer.doImport(
        new java.io.BufferedReader(
          new java.io.StringReader(
            org.nlogo.util.Utils.url2String(
              appletPanel.getFileURL(importer.filename).toString))))
    else
      super.doImport(importer)
  }
  override def inspectAgent(agent: org.nlogo.api.Agent, radius: Double) { }
  override def inspectAgent(agentClass: Class[_ <: Agent], agent: Agent, radius: Double) { }
  override def stopInspectingAgent(agent: org.nlogo.agent.Agent): Unit = { }
  override def stopInspectingDeadAgents(): Unit = { }
  override def closeAgentMonitors() { }
  override def newRenderer = Femto.get(
    classOf[RendererInterface], "org.nlogo.render.Renderer", Array(world))
}
