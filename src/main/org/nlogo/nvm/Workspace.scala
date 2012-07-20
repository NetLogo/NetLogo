// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api
import org.nlogo.agent.{ Agent, AgentSet, World }
import java.util.{ Map => JMap, WeakHashMap => JWeakHashMap }
import java.io.IOException

trait Workspace extends api.ImporterUser with JobManagerOwner with api.CompilerServices with api.RandomServices {
  def world: World
  def breathe() // called when engine comes up for air
  def joinForeverButtons(agent: Agent)
  def addJobFromJobThread(job: Job)
  def getExtensionManager: api.ExtensionManager
  def requestDisplayUpdate(force: Boolean)
  def getProcedures: JMap[String, Procedure]
  def setProcedures(procedures: JMap[String, Procedure])
  @throws(classOf[api.LogoException])
  def waitFor(runnable: api.CommandRunnable)
  @throws(classOf[api.LogoException])
  def waitForResult[T](runnable: api.ReporterRunnable[T]): T
  @throws(classOf[IOException])
  def importWorld(reader: java.io.Reader)
  @throws(classOf[IOException])
  def importWorld(path: String)
  @throws(classOf[IOException])
  def importDrawing(path: String)
  def clearDrawing()
  @throws(classOf[IOException])
  def exportDrawing(path: String, format: String)
  @throws(classOf[IOException])
  def exportView(path: String, format: String)
  def exportView: java.awt.image.BufferedImage
  @throws(classOf[IOException])
  def exportInterface(path: String)
  @throws(classOf[IOException])
  def exportWorld(path: String)
  @throws(classOf[IOException])
  def exportWorld(writer: java.io.PrintWriter)
  @throws(classOf[IOException])
  def exportOutput(path: String)
  @throws(classOf[IOException])
  def exportPlot(plotName: String, path: String)
  @throws(classOf[IOException])
  def exportAllPlots(path: String)
  def inspectAgent(agent: api.Agent, radius: Double)
  def inspectAgent(agentClass: Class[_ <: Agent], agent: Agent, radius: Double)
  def getAndCreateDrawing(): java.awt.image.BufferedImage
  @throws(classOf[api.LogoException])
  def waitForQueuedEvents()
  @throws(classOf[api.LogoException])
  def outputObject(obj: AnyRef, owner: AnyRef, addNewline: Boolean, readable: Boolean, destination: api.OutputDestination)
  def clearOutput()
  @throws(classOf[api.LogoException])
  def clearAll()
  @throws(classOf[api.CompilerException])
  def compileForRun(source: String, context: Context, reporter: Boolean): Procedure
  @throws(classOf[IOException])
  def convertToNormal(): String
  def getModelPath: String
  def setModelPath(path: String)
  def getModelDir: String
  def getModelFileName: String
  def fileManager: FileManager
  // kludgy this is AnyRef, but we don't want to have a compile-time dependency on org.nlogo.plot.
  // should be cleaned up sometime by introducing api.PlotManager? ST 2/12/08
  def plotManager: AnyRef
  def updatePlots(c: Context)
  def setupPlots(c: Context)
  def previewCommands: String
  def tick(c: Context, originalInstruction: Instruction)
  def resetTicks(c: Context)
  def clearTicks()
  @throws(classOf[java.net.MalformedURLException])
  def attachModelDir(filePath: String): String
  @throws(classOf[api.CompilerException])
  def evaluateCommands(owner: api.JobOwner, source: String)
  @throws(classOf[api.CompilerException])
  def evaluateCommands(owner: api.JobOwner, source: String, waitForCompletion: Boolean)
  @throws(classOf[api.CompilerException])
  def evaluateCommands(owner: api.JobOwner, source: String, agent: Agent, waitForCompletion: Boolean)
  @throws(classOf[api.CompilerException])
  def evaluateCommands(owner: api.JobOwner, source: String, agents: AgentSet, waitForCompletion: Boolean)
  @throws(classOf[api.CompilerException])
  def evaluateReporter(owner: api.JobOwner, source: String): AnyRef
  @throws(classOf[api.CompilerException])
  def evaluateReporter(owner: api.JobOwner, source: String, agent: Agent): AnyRef
  @throws(classOf[api.CompilerException])
  def evaluateReporter(owner: api.JobOwner, source: String, agents: AgentSet): AnyRef
  @throws(classOf[api.CompilerException])
  def compileCommands(source: String): Procedure
  @throws(classOf[api.CompilerException])
  def compileCommands(source: String, agentClass: Class[_ <: Agent]): Procedure
  @throws(classOf[api.CompilerException])
  def compileReporter(source: String): Procedure
  def runCompiledCommands(owner: api.JobOwner, procedure: Procedure): Boolean
  def runCompiledReporter(owner: api.JobOwner, procedure: Procedure): AnyRef
  @throws(classOf[InterruptedException])
  def dispose()
  def patchSize: Double
  def changeTopology(wrapX: Boolean, wrapY: Boolean)
  @throws(classOf[api.LogoException])
  @throws(classOf[IOException])
  @throws(classOf[api.CompilerException])
  def open(modelPath: String)
  @throws(classOf[api.LogoException])
  @throws(classOf[api.CompilerException])
  def openString(modelContents: String)
  def magicOpen(name: String)
  def changeLanguage()
  def compiler: CompilerInterface
  def isHeadless: Boolean
  def behaviorSpaceRunNumber: Int
  def behaviorSpaceRunNumber(n: Int)
  // for now this only works in HeadlessWorkspace, returns null in GUIWorkspace.  error handling
  // stuff is a mess, should be redone - ST 3/10/09, 1/22/12
  def lastLogoException: api.LogoException
  def clearLastLogoException()
  def lastRunTimes: JWeakHashMap[Job, JWeakHashMap[Agent, JWeakHashMap[Command, MutableLong]]]  // for _every
  def completedActivations: JWeakHashMap[Activation, java.lang.Boolean]  // for _thunkdidfinish
  def profilingEnabled: Boolean
  def profilingTracer: Tracer
}
