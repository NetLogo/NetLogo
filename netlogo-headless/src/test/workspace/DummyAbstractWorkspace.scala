// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.agent.{ Agent, World }
import org.nlogo.nvm, nvm.CompilerInterface
import org.nlogo.api
import org.nlogo.core, org.nlogo.core.{File, Model}

/**
 * handy for use in unit tests
 */

class DummyAbstractWorkspace
extends AbstractWorkspace(new World)
{
  dispose() // don't leak a JobThread - ST 5/2/13
  private def unsupported = throw new UnsupportedOperationException
  override def compilerTestingMode = false
  override def waitFor(runnable: api.CommandRunnable): Unit = unsupported
  override def waitForResult[T](runnable: api.ReporterRunnable[T]): T = unsupported
  override def waitForQueuedEvents(): Unit = unsupported
  override def inspectAgent(agent: api.Agent, radius: Double): Unit = unsupported
  override def inspectAgent(kind: core.AgentKind, agent: Agent, radius: Double): Unit = unsupported
  override def clearDrawing(): Unit = unsupported
  override def getAndCreateDrawing(): java.awt.image.BufferedImage = unsupported
  override def open(path: String) = unsupported
  override def openModel(model: Model) = unsupported
  override def clearOutput(): Unit = unsupported
  override def sendOutput(oo: org.nlogo.agent.OutputObject, toOutputArea: Boolean): Unit = unsupported
  override def importerErrorHandler: org.nlogo.agent.ImporterJ.ErrorHandler = unsupported
  override def importDrawing(file: File) = unsupported
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
  override def setDimensions(d: core.WorldDimensions) = unsupported
  override def setDimensions(d: core.WorldDimensions, patchSize: Double) = unsupported
  override def resizeView(): Unit = unsupported
  override def runtimeError(owner: api.JobOwner,
                            context: nvm.Context,
                            instruction: nvm.Instruction,
                            ex: Exception) = unsupported
  override def ownerFinished(owner: api.JobOwner) = unsupported
  override def updateDisplay(haveWorldLockAlready: Boolean): Unit = unsupported
  override def requestDisplayUpdate(force: Boolean) = unsupported
  override def breathe(context: nvm.Context): Unit = unsupported
  override def periodicUpdate(): Unit = unsupported
  override def addJobFromJobThread(job: nvm.Job) = unsupported
  override def compiler: CompilerInterface = unsupported
  override def renderer = unsupported
  override def command(source: String) = unsupported
  override def report(source: String) = unsupported
  override def worldChecksum = unsupported
  override def graphicsChecksum = unsupported

  // from ViewSettings
  override def drawSpotlight = unsupported
  override def fontSize = unsupported
  override def perspective = unsupported
  override def renderPerspective = unsupported
  override def viewHeight = unsupported
  override def viewOffsetX = unsupported
  override def viewOffsetY = unsupported
  override def viewWidth = unsupported
  override def isHeadless = true
}
