// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.core.{ AgentKind, CompilerException }
import org.nlogo.api.{ Agent => ApiAgent, ExportPlotWarningAction, JobOwner, Workspace => ApiWorkspace, MersenneTwisterFast }

import org.nlogo.agent.{ Agent, AgentSet, World }

import collection.mutable.WeakHashMap

trait Workspace extends ApiWorkspace with JobManagerOwner {

  def world: World

  def compiler: CompilerInterface

  def procedures: Procedure.ProceduresMap
  def procedures_=(procedures: Procedure.ProceduresMap): Unit

  def tick(c: Context, originalInstruction: Instruction): Unit
  def resetTicks(c: Context): Unit

  @throws(classOf[java.net.MalformedURLException])
  def attachModelDir(filePath: String): String

  def behaviorSpaceExperimentName: String
  def behaviorSpaceExperimentName(name: String): Unit

  def getComponent[A <: AnyRef](componentClass: Class[A]): Option[A]

  /** lastRunTimes is used by `every` to track how long ago a job ran */
  def lastRunTimes: WeakHashMap[Job, WeakHashMap[Agent, WeakHashMap[Command, MutableLong]]]
  /** completedActivations is used by `__thunk-did-finish` */
  def completedActivations: WeakHashMap[Activation, Boolean]

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

  /* components */
  def fileManager: FileManager
  def profilingTracer: Tracer

  /* plots */
  def plotRNG: MersenneTwisterFast
  def setupPlots(c: Context): Unit
  def updatePlots(c: Context): Unit
  def shouldUpdatePlots(): Boolean
  def setShouldUpdatePlots(shouldUpdatePlots: Boolean): Unit
  def triedToExportPlot(): Boolean
  def setTriedToExportPlot(triedToExport: Boolean): Unit
  def exportPlotWarningAction(): ExportPlotWarningAction
  def setExportPlotWarningAction(action: ExportPlotWarningAction): Unit

  /* job controls */
  def addJobFromJobThread(job: Job): Unit
  def joinForeverButtons(agent: Agent): Unit

  /* controls for things outside of nvm */
  def breathe(context: Context): Unit
  def requestDisplayUpdate(force: Boolean): Unit
  def inspectAgent(agent: ApiAgent, radius: Double): Unit
  def inspectAgent(agentKind: AgentKind, agent: Agent, radius: Double): Unit
  def stopInspectingAgent(agent: org.nlogo.agent.Agent): Unit
  def stopInspectingDeadAgents(): Unit
}

trait EditorWorkspace {
  def magicOpen(name: String): Unit

  @throws(classOf[java.io.IOException])
  def convertToNormal(): String
}
