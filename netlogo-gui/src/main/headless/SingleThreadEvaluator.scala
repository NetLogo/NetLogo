// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import java.util.ArrayList
import org.nlogo.agent.ArrayAgentSet
import org.nlogo.agent.{Agent, AgentSet, Observer, Turtle, Patch, Link}
import org.nlogo.api.{ JobOwner, LogoException, ReporterLogoThunk, CommandLogoThunk}
import org.nlogo.core.{ AgentKind, CompilerException }
import org.nlogo.nvm.{ExclusiveJob, Activation, Context, Procedure, SuspendableJob}
import org.nlogo.workspace.{ AbstractWorkspace, AbstractEvaluator, JobManagement }

import scala.collection.immutable.Vector
import scala.util.Try

class SingleThreadEvaluator(workspace: AbstractWorkspace)
  extends AbstractEvaluator(workspace) {
  def defaultAgentSet: AgentSet = workspace.world.observers
  def defaultWaitForCompletion: Boolean = true

  @throws(classOf[CompilerException])
  override def evaluateCommands(owner: JobOwner,
                       source: String,
                       agentSet: AgentSet = workspace.world.observers,
                       waitForCompletion: Boolean = true) = {
    val procedure = invokeCompiler(source, None, true, agentSet.kind)
    val suspendableJob = new SuspendableJob(agentSet, procedure, 0, null, workspace.mainRNG)
    if (waitForCompletion)
      suspendableJob.runFor(10000)
  }

  @throws(classOf[CompilerException])
  override def evaluateReporter(owner: JobOwner, source: String, agents: AgentSet = workspace.world.observers): Object = {
    val procedure = invokeCompiler(source, None, false, agents.kind)
    val suspendableJob = new SuspendableJob(agents, procedure, 0, null, workspace.mainRNG)
    suspendableJob.runResult()
  }

  @throws(classOf[CompilerException])
  override def compileCommands(source: String, agentClass: AgentKind): Procedure =
    invokeCompiler(source, None, true, agentClass)

  @throws(classOf[CompilerException])
  def compileReporter(source: String) =
    invokeCompiler(source, None, false, AgentKind.Observer)

  /**
   * @return whether the code did a "stop" at the top level
   */
  def runCompiledCommands(owner: JobOwner, procedure: Procedure) = {
    val suspendableJob = new SuspendableJob(workspace.world.observers, procedure, 0, null, workspace.mainRNG)
    suspendableJob.runFor(100000)
    suspendableJob.stopping
  }

  def runCompiledReporter(owner: JobOwner, procedure: Procedure) = {
    val suspendableJob = new SuspendableJob(workspace.world.observers, procedure, 0, null, workspace.mainRNG)
    suspendableJob.runResult
  }
}
