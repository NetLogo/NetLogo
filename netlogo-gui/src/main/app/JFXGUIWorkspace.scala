// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo(UTF8)

package org.nlogo.app

import org.nlogo.agent.World
import org.nlogo.api.{ControlSet, Exceptions, FileIO, ModelSettings}
import org.nlogo.nvm.PresentationCompilerInterface
import org.nlogo.workspace.AbstractWorkspaceScala

class JFXGUIWorkspace(world: World,
  val compiler: PresentationCompilerInterface)
  extends AbstractWorkspaceScala(world, null) {

  // Members declared in org.nlogo.workspace.AbstractWorkspace
  def aggregateManager(): org.nlogo.api.AggregateManagerInterface = ???
  def breathe(): Unit = {
    jobManager.maybeRunSecondaryJobs()
  }
  def clearDrawing(): Unit = ???
  override def importDrawing(x$1: org.nlogo.core.File): Unit = ???
  override def importerErrorHandler(): org.nlogo.agent.Importer.ErrorHandler = ???
  def magicOpen(x$1: String): Unit = ???
  def open(x$1: String): Unit = ???
  def openString(x$1: String): Unit = ???
  def requestDisplayUpdate(x$1: Boolean): Unit = ??? // TODO: this probably needs to be implemented
  override def sendOutput(x$1: org.nlogo.agent.OutputObject,x$2: Boolean): Unit = ???


  // Members declared in org.nlogo.workspace.AbstractWorkspaceScala
  def compilerTestingMode: Boolean = false
  def isHeadless: Boolean = false

  // Members declared in org.nlogo.workspace.Exporting
  def exportDrawingToCSV(writer: java.io.PrintWriter): Unit = ???
  def exportOutputAreaToCSV(writer: java.io.PrintWriter): Unit = ???

  // Members declared in org.nlogo.nvm.JobManagerOwner // TODO: These will probably need to be implemented before the model will run
  def ownerFinished(owner: org.nlogo.api.JobOwner): Unit = ???
  def periodicUpdate(): Unit = ???
  def runtimeError(owner: org.nlogo.api.JobOwner,context: org.nlogo.nvm.Context,instruction: org.nlogo.nvm.Instruction,ex: Exception): Unit = ???
  def updateDisplay(haveWorldLockAlready: Boolean): Unit = ???

  // Members declared in org.nlogo.nvm.LoggingWorkspace
  def deleteLogFiles(): Unit = ???
  def startLogging(properties: String): Unit = ???
  def zipLogFiles(filename: String): Unit = ???


  // Members declared in org.nlogo.nvm.Workspace
  def inspectAgent(agentKind: org.nlogo.core.AgentKind,agent: org.nlogo.agent.Agent,radius: Double): Unit = ???
  def inspectAgent(agent: org.nlogo.api.Agent,radius: Double): Unit = ???
  def stopInspectingAgent(agent: org.nlogo.agent.Agent): Unit = ???
  def stopInspectingDeadAgents(): Unit = ???

  // Members declared in org.nlogo.api.Workspace
  def changeTopology(wrapX: Boolean,wrapY: Boolean): Unit = ???
  def clearOutput(): Unit = ???
  def exportDrawing(path: String,format: String): Unit = ???
  def exportInterface(path: String): Unit = ???
  def exportOutput(path: String): Unit = ???
  def exportView: java.awt.image.BufferedImage = ???
  def exportView(path: String,format: String): Unit = ???
  def getAndCreateDrawing(): java.awt.image.BufferedImage = ???
  def openModel(model: org.nlogo.core.Model): Unit = ???
  def patchSize: Double = ???
  def renderer: org.nlogo.api.RendererInterface = ???
  def waitFor(runnable: org.nlogo.api.CommandRunnable): Unit = ???
  def waitForQueuedEvents(): Unit = ???
  def waitForResult[T](runnable: org.nlogo.api.ReporterRunnable[T]): T = ???

  // Members declared in org.nlogo.api.WorldResizer
  def patchSize(patchSize: Double): Unit = ???
  def resizeView(): Unit = ???
  def setDimensions(dim: org.nlogo.core.WorldDimensions,patchSize: Double): Unit = ???
  def setDimensions(dim: org.nlogo.core.WorldDimensions): Unit = ???
}
