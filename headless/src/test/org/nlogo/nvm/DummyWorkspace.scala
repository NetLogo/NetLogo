// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.agent.{Agent, AgentSet, World}
import org.nlogo.api
import org.nlogo.api.{WorldDimensions, DummyCompilerServices, DummyExtensionManager, JobOwner,
                      CommandRunnable, ReporterRunnable, ImportErrorHandler, OutputDestination}

class DummyWorkspace extends DummyCompilerServices with Workspace {
  private def unsupported = throw new UnsupportedOperationException
  val world = new World
  override def procedures = CompilerInterface.NoProcedures
  override def procedures_=(procedures: CompilerInterface.ProceduresMap) = unsupported
  override def requestDisplayUpdate(force: Boolean) = unsupported
  override def breathe() = unsupported
  override def joinForeverButtons(agent: Agent) = unsupported
  override def addJobFromJobThread(job: Job) = unsupported
  override def getExtensionManager() = new DummyExtensionManager
  override def waitFor(runnable: CommandRunnable) = unsupported
  override def waitForResult[T](runnable: ReporterRunnable[T]): T = unsupported
  override def importWorld(path: String) = unsupported
  override def importWorld(reader: java.io.Reader) = unsupported
  override def importDrawing(path: String) = unsupported
  override def clearDrawing() = unsupported
  override def exportDrawing(path: String, format: String) = unsupported
  override def exportView(path: String, format: String) = unsupported
  override def exportView() = unsupported
  override def exportInterface(path: String) = unsupported
  override def exportWorld(path: String) = unsupported
  override def exportWorld(writer: java.io.PrintWriter) = unsupported
  override def exportOutput(path: String) = unsupported
  override def exportPlot(plotName: String, path: String) = unsupported
  override def exportAllPlots(path: String) = unsupported
  override def inspectAgent(agent: api.Agent, radius: Double) = unsupported
  override def inspectAgent(kind: api.AgentKind, agent: Agent, radius: Double) = unsupported
  override def getAndCreateDrawing() = unsupported
  override def waitForQueuedEvents() = unsupported
  override def outputObject(obj: AnyRef, owner: AnyRef, addNewline: Boolean, readable: Boolean,
                   destination: OutputDestination) = unsupported
  override def clearOutput() = unsupported
  override def clearAll() = unsupported
  override def compileForRun(source: String, context: Context, reporter: Boolean) = unsupported
  override def convertToNormal() = unsupported
  override def getModelPath() = unsupported
  override def setModelPath(path: String) = unsupported
  override def getModelDir() = unsupported
  override def getModelFileName() = unsupported
  override def fileManager() = unsupported
  override def plotManager() = unsupported
  override def attachModelDir(filePath: String) = unsupported
  override def evaluateCommands(owner: JobOwner, source: String) = unsupported
  override def evaluateCommands(owner: JobOwner, source: String, waitForCompletion: Boolean) = unsupported
  override def evaluateCommands(owner: JobOwner, source: String, agent: Agent, waitForCompletion: Boolean) = unsupported
  override def evaluateCommands(owner: JobOwner, source: String, agents: AgentSet, waitForCompletion: Boolean) = unsupported
  override def evaluateReporter(owner: JobOwner, source: String) = unsupported
  override def evaluateReporter(owner: JobOwner, source: String, agent: Agent) = unsupported
  override def evaluateReporter(owner: JobOwner, source: String, agents: AgentSet) = unsupported
  override def compileCommands(source: String) = unsupported
  override def compileCommands(source: String, kind: api.AgentKind) = unsupported
  override def compileReporter(source: String) = unsupported
  override def runCompiledCommands(owner: JobOwner, procedure: Procedure) = unsupported
  override def runCompiledReporter(owner: JobOwner, procedure: Procedure) = unsupported
  override def patchSize() = unsupported
  override def changeTopology(wrapX: Boolean, wrapY: Boolean) = unsupported
  override def magicOpen(name: String) = unsupported
  override def changeLanguage() = unsupported
  override def lastRunTimes() = unsupported
  override def completedActivations() = unsupported
  override def compiler() = unsupported
  override def open(modelPath: String) = unsupported
  override def openString(modelContents: String) = unsupported
  override def dispose() { }
  override def lastLogoException() = unsupported
  override def clearLastLogoException() = unsupported
  override def isHeadless() = unsupported
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

  // from JobManagerOwner
  override def runtimeError(owner: JobOwner, context: Context, instruction: Instruction, ex: Exception) = unsupported
  override def ownerFinished(owner: JobOwner) = unsupported
  override def updateDisplay(haveWorldLockAlready: Boolean) = unsupported
  override def periodicUpdate() = unsupported

  // from RandomServices
  override def auxRNG = null
  override def mainRNG = null

  override def profilingEnabled = false
  override def profilingTracer = unsupported

  override def tick(c:Context, i:Instruction) = unsupported
  override def resetTicks(c:Context) = unsupported
  override def clearTicks = unsupported
  override def setupPlots(c:Context) = unsupported
  override def updatePlots(c:Context) = unsupported
}
