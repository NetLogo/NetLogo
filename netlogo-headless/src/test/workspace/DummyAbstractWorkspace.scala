// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.agent.Agent
import org.nlogo.nvm
import org.nlogo.api
import org.nlogo.core, org.nlogo.core.{File, Model}

/**
 * handy for use in unit tests
 */

class DummyAbstractWorkspace(helper: Helper)
extends AbstractWorkspace(helper)
with HeadlessCatchAll
with DefaultWorldLoader {
  def this() = this(Helper.twoD)
  dispose() // don't leak a JobThread - ST 5/2/13
  override def silent: Boolean = true
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
  override def openString(modelContents: String) = unsupported
  override def openModel(model: Model) = unsupported
  override def clearOutput(): Unit = unsupported
  override def disablePeriodicRendering(): Unit = {}
  override def enablePeriodicRendering(): Unit = {}
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
  override def setDimensions(dim: org.nlogo.core.WorldDimensions,showProgress: Boolean,stop: org.nlogo.api.WorldResizer.JobStop): Unit = unsupported
  override def resizeView(): Unit = unsupported
  override def requestDisplayUpdate(force: Boolean) = unsupported
  override def breathe(context: nvm.Context): Unit = unsupported
  override def addJobFromJobThread(job: nvm.Job) = unsupported
  override def updateDisplay(haveWorldLockAlready: Boolean,forced: Boolean): Unit = unsupported
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

  // Members declared in org.nlogo.workspace.DefaultWorldLoader
  def getMinimumWidth: Int = unsupported
  def insetWidth: Int = unsupported

  // Members declared in org.nlogo.workspace.WorldLoaderInterface
  def clearTurtles(): Unit = unsupported
  def frameRate(rate: Double): Unit = unsupported
  def frameRate: Double = unsupported
  def setSize(x: Int,y: Int): Unit = unsupported
  def showTickCounter: Boolean = unsupported
  def showTickCounter(visible: Boolean): Unit = unsupported
  def tickCounterLabel: String = unsupported
  def tickCounterLabel(label: String): Unit = unsupported
  def updateMode(updateMode: org.nlogo.core.UpdateMode): Unit = unsupported
}
