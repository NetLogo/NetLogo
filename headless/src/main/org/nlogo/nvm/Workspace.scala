// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api
import org.nlogo.agent.{ Agent, AgentSet, World }
import java.util.{ WeakHashMap => JWeakHashMap }
import java.io.IOException

trait Workspace extends api.Workspace with JobManagerOwner {
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

  @throws(classOf[api.CompilerException])
  def compileForRun(source: String, context: Context, reporter: Boolean): Procedure
  @throws(classOf[api.CompilerException])
  def compileCommands(source: String): Procedure
  @throws(classOf[api.CompilerException])
  def compileCommands(source: String, kind: api.AgentKind): Procedure
  @throws(classOf[api.CompilerException])
  def compileReporter(source: String): Procedure
  def runCompiledCommands(owner: api.JobOwner, procedure: Procedure): Boolean
  def runCompiledReporter(owner: api.JobOwner, procedure: Procedure): AnyRef

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

}
