// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api
import org.nlogo.agent.{ Agent, AgentSet, World }
import java.util.{ WeakHashMap => JWeakHashMap }
import java.io.IOException

trait Workspace extends api.Workspace with JobManagerOwner {
  def breathe(context: Context) // called when engine comes up for air
  def requestDisplayUpdate(context: Context, force: Boolean)
  def updateUI(context: Context) { }
  def joinForeverButtons(agent: Agent)
  def addJobFromJobThread(job: Job)
  def procedures: CompilerInterface.ProceduresMap
  def procedures_=(procedures: CompilerInterface.ProceduresMap)
  def fileManager: FileManager
  def tick(c: Context, originalInstruction: Instruction)
  def compiler: CompilerInterface
  def lastRunTimes: JWeakHashMap[Job, JWeakHashMap[Agent, JWeakHashMap[Command, MutableLong]]]  // for _every
  def completedActivations: JWeakHashMap[Activation, java.lang.Boolean]  // for _thunkdidfinish
  def profilingTracer: Tracer
  def updatePlots(c: Context)
  def setupPlots(c: Context)
  def resetTicks(c: Context)
  def inspectAgent(agent: Agent, radius: Double)
  def inspectAgent(kind: api.AgentKind, agent: Agent, radius: Double)

  def compileForRun(source: String, context: Context, reporter: Boolean): Procedure
  def compileCommands(source: String): Procedure
  def compileCommands(source: String, kind: api.AgentKind): Procedure
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
