// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.agent.{Agent, World}
import org.nlogo.nvm.CompilerInterface
import org.nlogo.api

/**
 * handy for use in unit tests
 */

class DummyAbstractWorkspace
extends AbstractWorkspaceScala(new World)
{
  private def unsupported = throw new UnsupportedOperationException
  override val isHeadless = true
  override def compilerTestingMode = false
  override def waitFor(runnable: api.CommandRunnable): Unit = unsupported
  override def waitForResult[T](runnable: api.ReporterRunnable[T]): T = unsupported
  override def waitForQueuedEvents(): Unit = unsupported
  override def inspectAgent(agent: Agent, radius: Double): Unit = unsupported
  override def inspectAgent(kind: api.AgentKind, agent: Agent, radius: Double): Unit = unsupported
  override def clearDrawing(): Unit = unsupported
  override def getAndCreateDrawing(): java.awt.image.BufferedImage = unsupported
  override def open(path: String) = unsupported
  override def openString(modelContents: String) = unsupported
  override def magicOpen(name: String) = unsupported
  override def changeLanguage() = unsupported
  override def clearOutput(): Unit = unsupported
  override def sendOutput(oo: org.nlogo.agent.OutputObject, toOutputArea: Boolean): Unit = unsupported
  override def importerErrorHandler: org.nlogo.agent.ImporterJ.ErrorHandler = unsupported
  override def importDrawing(file: api.File) = unsupported
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
  override def setOutputAreaContents(text: String) = unsupported
  override def setDimensions(d: api.WorldDimensions) = unsupported
  override def setDimensions(d: api.WorldDimensions, patchSize: Double) = unsupported
  override def resizeView(): Unit = unsupported
  override def runtimeError(owner: api.JobOwner,
                            context: org.nlogo.nvm.Context,
                            instruction: org.nlogo.nvm.Instruction,
                            ex: Exception) = unsupported
  override def ownerFinished(owner: api.JobOwner) = unsupported
  override def updateDisplay(haveWorldLockAlready: Boolean): Unit = unsupported
  override def requestDisplayUpdate(force: Boolean) = unsupported
  override def breathe(): Unit = unsupported
  override def periodicUpdate(): Unit = unsupported
  override def addJobFromJobThread(job: org.nlogo.nvm.Job) = unsupported
  override def compiler: CompilerInterface = unsupported
}
