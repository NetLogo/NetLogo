// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.core.{ AgentKind, CompilerException, ProcedureSyntax, Token }
import org.nlogo.api.{ Agent => ApiAgent, CompilerServices, JobOwner, Workspace => ApiWorkspace }

import org.nlogo.agent.{ Agent, AgentSet, World }

import collection.mutable.WeakHashMap

trait Workspace
  extends ApiWorkspace
  with CompilerServices {

  def world: World

  def compiler: CompilerInterface

  def procedures: Procedure.ProceduresMap

  def tick(c: Context, originalInstruction: Instruction)
  def resetTicks(c: Context)

  def behaviorSpaceExperimentName: String
  def behaviorSpaceExperimentName(name: String): Unit

  def owner: JobManagerOwner

  def getComponent[A <: AnyRef](componentClass: Class[A]): Option[A]

  /** lastRunTimes is used by `every` to track how long ago a job ran */
  def lastRunTimes: WeakHashMap[Job, WeakHashMap[Agent, WeakHashMap[Command, MutableLong]]]

  /* for lab.Worker */
  def updateDisplay(haveWorldLockAlready: Boolean, forced: Boolean): Unit

  /* compiler controls */
  @throws(classOf[CompilerException])
  def compileCommands(source: String): Procedure
  @throws(classOf[CompilerException])
  def compileCommands(source: String, agentKind: AgentKind): Procedure
  @throws(classOf[CompilerException])
  def compileForRun(source: String, context: Context, reporter: Boolean): Procedure
  @throws(classOf[CompilerException])
  def compileReporter(source: String): Procedure

  /* evaluation */
  def runCompiledCommands(owner: JobOwner, procedure: Procedure): Boolean
  def runCompiledReporter(owner: JobOwner, procedure: Procedure): AnyRef

  @throws(classOf[CompilerException])
  def evaluateCommands(owner: JobOwner, source: String, agent: Agent, waitForCompletion: Boolean): Unit
  @throws(classOf[CompilerException])
  def evaluateCommands(owner: JobOwner, source: String, agents: AgentSet, waitForCompletion: Boolean): Unit

  @throws(classOf[CompilerException])
  def evaluateReporter(owner: JobOwner, source: String, agent: Agent): AnyRef
  @throws(classOf[CompilerException])
  def evaluateReporter(owner: JobOwner, source: String, agents: AgentSet): AnyRef

  def linker: Linker

  /* components */
  def fileManager: FileManager
  def profilingTracer: Tracer
  def modelTracker: ModelTracker

  /* plots */
  def setupPlots(c: Context): Unit
  def updatePlots(c: Context): Unit

  /* job controls */
  def addJobFromJobThread(job: Job)
  def joinForeverButtons(agent: Agent)

  /* controls for things outside of nvm */
  def breathe(context: Context): Unit
  def disablePeriodicRendering(): Unit
  def enablePeriodicRendering(): Unit
  def requestDisplayUpdate(force: Boolean)
  def inspectAgent(agent: ApiAgent, radius: Double)
  def inspectAgent(agentKind: AgentKind, agent: Agent, radius: Double): Unit
  def stopInspectingAgent(agent: org.nlogo.agent.Agent): Unit
  def stopInspectingDeadAgents(): Unit

  @deprecated("Workspace.checkCommandSyntax is deprecated, use Workspace.compilerServices.checkCommandSyntax", "6.1.0")
  def checkCommandSyntax(source: String): Unit
  @deprecated("Workspace.checkReporterSyntax is deprecated, use Workspace.compilerServices.checkReporterSyntax", "6.1.0")
  def checkReporterSyntax(source: String): Unit
  @deprecated("Workspace.findProcedurePositions is deprecated, use Workspace.compilerServices.findProcedurePositions", "6.1.0")
  def findProcedurePositions(source: String): Map[String, ProcedureSyntax]
  @deprecated("Workspace.getTokenAtPosition is deprecated, use Workspace.compilerServices.getTokenAtPosition", "6.1.0")
  def getTokenAtPosition(source: String,position: Int): Token
  @deprecated("Workspace.isConstant is deprecated, use Workspace.compilerServices.isConstant", "6.1.0")
  def isConstant(s: String): Boolean
  @deprecated("Workspace.isReporter is deprecated, use Workspace.compilerServices.isReporter", "6.1.0")
  def isReporter(s: String): Boolean
  @deprecated("Workspace.isValidIdentifier is deprecated, use Workspace.compilerServices.isValidIdentifier", "6.1.0")
  def isValidIdentifier(s: String): Boolean
  @deprecated("Workspace.tokenizeForColorization is deprecated, use Workspace.compilerServices.tokenizeForColorization", "6.1.0")
  def tokenizeForColorization(source: String): Array[Token]
  @deprecated("Workspace.tokenizeForColorizationIterator is deprecated, use Workspace.compilerServices.tokenizeForColorizationIterator", "6.1.0")
  def tokenizeForColorizationIterator(source: String): Iterator[Token]
}

trait EditorWorkspace {
  def magicOpen(name: String): Unit

  @throws(classOf[java.io.IOException])
  def convertToNormal(): String
}

trait LoggingWorkspace {
  def startLogging(properties: String): Unit
  def zipLogFiles(filename: String): Unit
  def deleteLogFiles(): Unit
}

trait Linker {
  def link(p: Procedure): Procedure
}

