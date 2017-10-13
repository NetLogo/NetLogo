// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.core.AgentKind
import org.nlogo.api.{ NetLogoLegacyDialect, NetLogoThreeDDialect }

/**
 * handy for use in unit tests
 */

class DummyAbstractWorkspace(helper: Helper)
extends AbstractWorkspace(helper) {
  def this(is3D: Boolean) = this(Helper.withDialect(if (is3D) NetLogoThreeDDialect else NetLogoLegacyDialect))
  dispose() // don't leak a JobThread - ST 5/2/13
  private def unsupported = throw new UnsupportedOperationException
  override val isHeadless = true
  override def compilerTestingMode = false
  override def waitFor(runnable: org.nlogo.api.CommandRunnable): Unit = unsupported
  override def waitForResult[T](runnable: org.nlogo.api.ReporterRunnable[T]): T = unsupported
  override def waitForQueuedEvents(): Unit = unsupported
  override def inspectAgent(agent: org.nlogo.api.Agent, radius: Double): Unit = unsupported
  override def inspectAgent(agentClass: AgentKind, agent: org.nlogo.agent.Agent, radius: Double): Unit = unsupported
  override def stopInspectingAgent(agent: org.nlogo.agent.Agent) = unsupported
  override def stopInspectingDeadAgents() = unsupported
  override def clearDrawing(): Unit = unsupported
  override def getAndCreateDrawing(): java.awt.image.BufferedImage = unsupported
  override def open(path: String) = unsupported
  override def openString(modelContents: String) = unsupported
  override def magicOpen(name: String) = unsupported
  override def clearOutput(): Unit = unsupported
  override def disablePeriodicRendering(): Unit = {}
  override def enablePeriodicRendering(): Unit = {}
  override def sendOutput(oo: org.nlogo.agent.OutputObject, toOutputArea: Boolean): Unit = unsupported
  override def importerErrorHandler: org.nlogo.agent.ImporterJ.ErrorHandler = unsupported
  override def importDrawing(file: org.nlogo.core.File) = unsupported
  override def exportOutput(filename: String) = unsupported
  override def exportDrawing(filename: String, format: String) = unsupported
  override def exportDrawingToCSV(writer: java.io.PrintWriter) = unsupported
  override def exportOutputAreaToCSV(writer: java.io.PrintWriter) = unsupported
  override def exportView(filename: String, format: String) = unsupported
  override def exportView: java.awt.image.BufferedImage = unsupported
  override def exportInterface(filename: String) = unsupported
  override def patchSize(patchSize: Double) = unsupported
  override def patchSize: Double = unsupported
  override def changeTopology(wrapX: Boolean, wrapY: Boolean) = unsupported
  override def requestDisplayUpdate(force: Boolean): Unit = unsupported
  override def setOutputAreaContents(text: String) = unsupported
  override def setDimensions(d: org.nlogo.core.WorldDimensions) = unsupported
  override def setDimensions(d: org.nlogo.core.WorldDimensions, patchSize: Double) = unsupported
  override def setDimensions(dim: org.nlogo.core.WorldDimensions,showProgress: Boolean,stop: org.nlogo.api.WorldResizer.JobStop): Unit = unsupported
  override def resizeView(): Unit = unsupported
  override def breathe(context: org.nlogo.nvm.Context): Unit = unsupported
  override def addJobFromJobThread(job: org.nlogo.nvm.Job) = unsupported
  override def startLogging(properties: String) = unsupported
  override def updateDisplay(haveWorldLockAlready: Boolean,forced: Boolean): Unit = unsupported
  override def zipLogFiles(filename: String) = unsupported
  override def deleteLogFiles(): Unit = unsupported

  def openModel(model: org.nlogo.core.Model): Unit = unsupported
  def renderer: org.nlogo.api.RendererInterface = unsupported
}
