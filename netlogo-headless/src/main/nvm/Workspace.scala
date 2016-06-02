// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.{ api, core },
  core.LiteralParser
import org.nlogo.agent.{ Agent, AgentSet }
import collection.mutable.WeakHashMap

trait Workspace extends api.Workspace with JobManagerOwner with api.ViewSettings {
  def world: org.nlogo.agent.World
  def breathe(context: Context) // called when engine comes up for air
  def requestDisplayUpdate(context: Context, force: Boolean)
  def updateUI(context: Context) { }
  def joinForeverButtons(agent: Agent)
  def addJobFromJobThread(job: Job)
  def procedures: Procedure.ProceduresMap
  def procedures_=(procedures: Procedure.ProceduresMap)
  def fileManager: FileManager
  def tick(c: Context, originalInstruction: Instruction)
  def compiler: CompilerInterface
  def parser: LiteralParser
  def lastRunTimes: WeakHashMap[Job, WeakHashMap[Agent, WeakHashMap[Command, MutableLong]]]  // for _every
  def completedActivations: WeakHashMap[Activation, Boolean]  // for _thunkdidfinish
  def profilingTracer: Tracer
  def updatePlots(c: Context)
  def setupPlots(c: Context)
  def resetTicks(c: Context)
  def inspectAgent(agent: Agent, radius: Double)
  def inspectAgent(kind: core.AgentKind, agent: Agent, radius: Double)

  def compileForRun(source: String, context: Context, reporter: Boolean): Procedure
  def compileCommands(source: String): Procedure
  def compileCommands(source: String, kind: core.AgentKind): Procedure
  def compileReporter(source: String): Procedure
  def runCompiledCommands(owner: api.JobOwner, procedure: Procedure): Boolean
  def runCompiledReporter(owner: api.JobOwner, procedure: Procedure): AnyRef

  def evaluateCommands(owner: api.JobOwner, source: String)
  def evaluateCommands(owner: api.JobOwner, source: String, waitForCompletion: Boolean)
  def evaluateCommands(owner: api.JobOwner, source: String, agent: Agent, waitForCompletion: Boolean)
  def evaluateCommands(owner: api.JobOwner, source: String, agents: AgentSet, waitForCompletion: Boolean)
  def evaluateReporter(owner: api.JobOwner, source: String): AnyRef
  def evaluateReporter(owner: api.JobOwner, source: String, agent: Agent): AnyRef
  def evaluateReporter(owner: api.JobOwner, source: String, agents: AgentSet): AnyRef

}
