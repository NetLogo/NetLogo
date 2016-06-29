// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.core.{ AgentKind, CompilationEnvironment, CompilerException }
import org.nlogo.api.{ AggregateManagerInterface, CommandRunnable, CompilerServices,
  HubNetInterface, ImporterUser, JobOwner, LogoException, OutputDestination,
  PreviewCommands, RandomServices, ReporterRunnable }

import org.nlogo.agent.{ Agent, AgentSet, World }

import java.util.Map
import java.util.WeakHashMap

trait Workspace extends org.nlogo.api.Workspace
  with ImporterUser
  with JobManagerOwner
  with CompilerServices
  with RandomServices {

  def world: org.nlogo.agent.World

  def fileManager: FileManager
  def getHubNetManager: Option[HubNetInterface]

  def runCompiledCommands(owner: JobOwner, procedure: Procedure): Boolean
  def runCompiledReporter(owner: JobOwner, procedure: Procedure): AnyRef

  def tick(c: Context, originalInstruction: Instruction): Unit
  def resetTicks(c: Context): Unit
  def clearTicks(): Unit

  def compiler: CompilerInterface
  def getCompilationEnvironment: CompilationEnvironment
  def getProcedures: java.util.Map[String, Procedure]

  @throws(classOf[CompilerException])
  def compileCommands(source: String): Procedure
  @throws(classOf[CompilerException])
  def compileCommands(source: String, agentKind: AgentKind): Procedure
  @throws(classOf[CompilerException])
  def compileReporter(source: String): Procedure
  @throws(classOf[CompilerException])
  def compileForRun(source: String, context: Context, reporter: Boolean): Procedure

  @throws(classOf[java.io.IOException])
  @throws(classOf[CompilerException])
  @throws(classOf[LogoException])
  def open(modelPath: String): Unit
  @throws(classOf[CompilerException])
  @throws(classOf[LogoException])
  def openString(modelContents: String): Unit
  def magicOpen(name: String): Unit

  def completedActivations: WeakHashMap[Activation, Boolean]

  def inspectAgent(agent: org.nlogo.api.Agent, radius: Double): Unit
  def inspectAgent(agentKind: AgentKind, agent: Agent, radius: Double): Unit
  def stopInspectingAgent(agent: org.nlogo.agent.Agent): Unit;
  def stopInspectingDeadAgents(): Unit

  def updatePlots(c: Context): Unit
  def setupPlots(c: Context): Unit

  def joinForeverButtons(agent: Agent): Unit

  def requestDisplayUpdate(force: Boolean): Unit

  def startLogging(properties: String): Unit
  def zipLogFiles(filename: String): Unit
  def deleteLogFiles(): Unit

  def behaviorSpaceExperimentName: String
  def behaviorSpaceExperimentName(name: String): Unit

  def aggregateManager: AggregateManagerInterface;

  def profilingTracer: Tracer

  // for _every
  def lastRunTimes: WeakHashMap[Job, WeakHashMap[Agent, WeakHashMap[Command, MutableLong]]]

  @throws(classOf[java.io.IOException])
  def convertToNormal(): String

  // called when engine comes up for air
  def breathe(): Unit

  @throws(classOf[java.net.MalformedURLException])
  def attachModelDir(filePath: String): String

  def addJobFromJobThread(job: Job): Unit

  @throws(classOf[InterruptedException])
  def dispose()

  @throws(classOf[CompilerException])
  def evaluateCommands(owner: JobOwner, source: String, agent: Agent, waitForCompletion: Boolean): Unit
  @throws(classOf[CompilerException])
  def evaluateCommands(owner: JobOwner, source: String, agents: AgentSet, waitForCompletion: Boolean): Unit

  @throws(classOf[CompilerException])
  def evaluateReporter(owner: JobOwner, source: String, agent: org.nlogo.agent.Agent): AnyRef
  @throws(classOf[CompilerException])
  def evaluateReporter(owner: JobOwner, source: String, agents: org.nlogo.agent.AgentSet): AnyRef
}
