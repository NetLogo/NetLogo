// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.core.{ AgentKind, DummyCompilationEnvironment }
import org.nlogo.agent.{Agent, AgentSet, World2D}
import org.nlogo.api.{ JobOwner, CommandRunnable,
  ReporterRunnable, ImportErrorHandler, OutputDestination}
import org.nlogo.core.WorldDimensions
import scala.collection.immutable.ListMap

class DummyWorkspace extends Workspace {
  private def unsupported = throw new UnsupportedOperationException
  val world = new World2D()
  override def procedures: ListMap[String,Procedure] = ListMap.empty[String, Procedure]
  override def joinForeverButtons(agent: Agent) = unsupported
  override def addJobFromJobThread(job: Job) = unsupported
  override def getExtensionManager = unsupported
  override def getCompilationEnvironment = new DummyCompilationEnvironment
  override def waitFor(runnable: CommandRunnable) = unsupported
  override def waitForResult[T](runnable: ReporterRunnable[T]): T = unsupported
  override def importWorld(path: String) = unsupported
  override def importWorld(reader: java.io.Reader) = unsupported
  override def importDrawing(path: String) = unsupported
  override def clearDrawing() = unsupported
  override def exportDrawing(path: String, format: String) = unsupported
  override def exportView(path: String, format: String) = unsupported
  override def exportView = unsupported
  override def exportInterface(path: String) = unsupported
  override def exportWorld(path: String) = unsupported
  override def exportWorld(writer: java.io.PrintWriter) = unsupported
  override def exportOutput(path: String) = unsupported
  override def exportPlot(plotName: String, path: String) = unsupported
  override def exportAllPlots(path: String) = unsupported
  override def getAndCreateDrawing = unsupported
  override def waitForQueuedEvents() = unsupported
  override def outputObject(obj: AnyRef, owner: AnyRef, addNewline: Boolean, readable: Boolean,
                   destination: OutputDestination) = unsupported
  override def clearOutput() = unsupported
  override def clearAll() = unsupported
  override def compileForRun(source: String, context: Context, reporter: Boolean) = unsupported
  override def getModelPath = unsupported
  override def setModelPath(path: String) = unsupported
  override def getModelDir = unsupported
  override def getModelFileName = unsupported
  override def fileManager = unsupported
  override def plotManager = unsupported
  override def modelTracker = unsupported
  override def evaluateCommands(owner: JobOwner, source: String, agents: AgentSet, waitForCompletion: Boolean) = unsupported
  override def compileCommands(source: String) = unsupported
  override def compileCommands(source: String,  agentClass: AgentKind) = unsupported
  override def evaluateCommands(owner: org.nlogo.api.JobOwner,source: String,agent: org.nlogo.agent.Agent,waitForCompletion: Boolean): Unit = unsupported
  override def evaluateReporter(owner: org.nlogo.api.JobOwner,source: String,agents: org.nlogo.agent.AgentSet): AnyRef = unsupported
  override def evaluateReporter(owner: org.nlogo.api.JobOwner,source: String,agent: org.nlogo.agent.Agent): AnyRef = unsupported
  override def evaluateCommands(owner: org.nlogo.api.JobOwner,source: String,waitForCompletion: Boolean): Unit = unsupported
  override def evaluateCommands(owner: org.nlogo.api.JobOwner,source: String): Unit = unsupported
  override def evaluateReporter(owner: org.nlogo.api.JobOwner,source: String): AnyRef = unsupported
  override def compileReporter(source: String) = unsupported
  override def runCompiledCommands(owner: JobOwner, procedure: Procedure) = unsupported
  override def runCompiledReporter(owner: JobOwner, procedure: Procedure) = unsupported
  override def patchSize = unsupported
  override def changeTopology(wrapX: Boolean, wrapY: Boolean) = unsupported
  override def lastRunTimes = unsupported
  override def compiler = unsupported
  override def open(modelPath: String) = unsupported
  override def dispose() { }
  override def lastLogoException = unsupported
  override def clearLastLogoException() = unsupported
  override def isHeadless = unsupported
  override def behaviorSpaceRunNumber = 0
  override def behaviorSpaceRunNumber(n: Int) = unsupported
  override def previewCommands = unsupported

  // from ImporterUser
  override def setOutputAreaContents(text: String) = unsupported
  override def currentPlot(plot: String) = unsupported
  override def getPlot(plot: String) = unsupported
  override def isExtensionName(name: String) = unsupported
  override def importExtensionData(name: String, data: java.util.List[Array[String]], handler: ImportErrorHandler) = unsupported

  // from WorldResizer
  override def resizeView() = unsupported
  override def patchSize(patchSize: Double) = unsupported
  override def setDimensions(dim: WorldDimensions) = unsupported
  override def setDimensions(dim: WorldDimensions, patchSize: Double) = unsupported
  override def setDimensions(dim: org.nlogo.core.WorldDimensions,showProgress: Boolean,stop: org.nlogo.api.WorldResizer.JobStop): Unit = unsupported

  // from JobManagerOwner
  override def runtimeError(owner: JobOwner, manager: JobManagerInterface, context: Context, instruction: Instruction, ex: Exception) = unsupported
  override def ownerFinished(owner: JobOwner) = unsupported
  override def periodicUpdate() = unsupported
  override def updateDisplay(haveWorldLockAlready: Boolean,forced: Boolean): Unit = unsupported

  // from RandomServices
  override def auxRNG = null
  override def mainRNG = null
  override def seedRNGs(seed: Int): Unit = unsupported

  override def profilingEnabled = false
  override def profilingTracer = unsupported

  override def tick(c:Context, i:Instruction) = unsupported
  override def resetTicks(c:Context) = unsupported
  override def clearTicks = unsupported
  override def setupPlots(c:Context) = unsupported
  override def updatePlots(c:Context) = unsupported

  // Members declared in org.nlogo.api.Controllable
  def command(source: String): Unit = unsupported
  def report(source: String): AnyRef = unsupported

  // Members declared in org.nlogo.api.ViewSettings
  def drawSpotlight: Boolean = unsupported
  def fontSize: Int = unsupported
  def perspective: org.nlogo.api.Perspective = unsupported
  def renderPerspective: Boolean = unsupported
  def viewHeight: Double = unsupported
  def viewOffsetX: Double = unsupported
  def viewOffsetY: Double = unsupported
  def viewWidth: Double = unsupported

  // Members declared in org.nlogo.api.Workspace
  def benchmark(minTime: Int,maxTime: Int): Unit = unsupported
  def compilerTestingMode: Boolean = unsupported
  def graphicsChecksum: String = unsupported
  def openModel(model: org.nlogo.core.Model): Unit = unsupported
  def renderer: org.nlogo.api.RendererInterface = unsupported
  def warningMessage(message: String): Boolean = unsupported
  def worldChecksum: String = unsupported
  def handleModelChange(): Unit = unsupported
  def writeOutputObject(x$1: org.nlogo.agent.OutputObject): Unit = unsupported

  // Members declared in org.nlogo.core.LiteralParser
  def readFromString(s: String): AnyRef = unsupported
  def readNumberFromString(source: String): AnyRef = unsupported

  // Members declared in org.nlogo.nvm.Workspace
  override def breathe(context: org.nlogo.nvm.Context): Unit = unsupported
  override def inspectAgent(agent: org.nlogo.api.Agent,radius: Double): Unit = unsupported
  override def inspectAgent(agentKind: org.nlogo.core.AgentKind, agent: Agent,radius: Double): Unit = unsupported
  override def stopInspectingAgent(agent: org.nlogo.agent.Agent): Unit = unsupported
  override def stopInspectingDeadAgents(): Unit = unsupported
  override def procedures_=(procedures: org.nlogo.nvm.Procedure.ProceduresMap): Unit = unsupported
  override def behaviorSpaceExperimentName(name: String): Unit = unsupported
  override def behaviorSpaceExperimentName: String = unsupported
  override def getComponent[A <: AnyRef](componentClass: Class[A]): Option[A] = unsupported
  override def requestDisplayUpdate(force: Boolean): Unit = unsupported
  override def disablePeriodicRendering(): Unit = unsupported
  override def enablePeriodicRendering(): Unit = unsupported
}
