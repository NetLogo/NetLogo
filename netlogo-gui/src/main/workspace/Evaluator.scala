// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.util.ArrayList
import org.nlogo.agent.ArrayAgentSet
import org.nlogo.agent.{Agent, AgentSet, Observer, Turtle, Patch, Link}
import org.nlogo.api.{ JobOwner, LogoException, ReporterLogoThunk, CommandLogoThunk}
import org.nlogo.core.{ AgentKind, CompilerException }
import org.nlogo.nvm.{ExclusiveJob, Activation, Context, Procedure}

import scala.collection.immutable.Vector
import scala.util.Try

class Evaluator(workspace: AbstractWorkspace) {

  @throws(classOf[CompilerException])
  def evaluateCommands(owner: JobOwner,
                       source: String,
                       agentSet: AgentSet = workspace.world.observers,
                       waitForCompletion: Boolean = true) = {
    val procedure = invokeCompiler(source, None, true, agentSet.kind)
    workspace.jobManager.addJob(
      workspace.jobManager.makeConcurrentJob(owner, agentSet, workspace, procedure),
      waitForCompletion)
  }

  @throws(classOf[CompilerException])
  def evaluateReporter(owner: JobOwner, source: String, agents: AgentSet = workspace.world.observers): Object = {
    val procedure = invokeCompiler(source, None, false, agents.kind)
    workspace.jobManager.addReporterJobAndWait(owner, agents, workspace, procedure)
  }

  @throws(classOf[CompilerException])
  def compileCommands(source: String, agentClass: AgentKind): Procedure =
    invokeCompiler(source, None, true, agentClass)

  @throws(classOf[CompilerException])
  def compileReporter(source: String) =
    invokeCompiler(source, None, false, AgentKind.Observer)

  /**
   * @return whether the code did a "stop" at the top level
   */
  def runCompiledCommands(owner: JobOwner, procedure: Procedure) = {
    val job = workspace.jobManager.makeConcurrentJob(owner, workspace.world.observers, workspace, procedure)
    workspace.jobManager.addJob(job, true)
    job.stopping
  }

  def runCompiledReporter(owner: JobOwner, procedure: Procedure) =
    workspace.jobManager.addReporterJobAndWait(owner, workspace.world.observers, workspace, procedure)

  ///

  @throws(classOf[CompilerException])
  def compileForRun(source: String, context: Context,reporter: Boolean) =
    invokeCompilerForRun(source, context.agent.kind, context.activation.procedure, reporter)

  ///

  private[workspace] def withContext(context: Context)(f: => Unit) {
    val oldContext = ProcedureRunner.context
    ProcedureRunner.context = context
    try f
    finally ProcedureRunner.context = oldContext
  }

  private object ProcedureRunner {
    var context: Context = null
    def run(p: Procedure, ownerOption: Option[JobOwner] = None): Try[Boolean] = {
      val oldActivation = context.activation
      val newActivation = new Activation(p, context.activation, 1)
      val oldRandom = context.job.random
      context.activation = newActivation
      context.job.random = ownerOption.map(_.random).getOrElse(workspace.world.mainRNG.clone)
      val procedureResult = Try {
        context.runExclusiveJob(workspace.world.observers, 0)
        val stopped = workspace.completedActivations.get(newActivation) != true
        stopped
      }
      context.activation = oldActivation
      context.job.random = oldRandom
      procedureResult
    }
  }

  @throws(classOf[CompilerException])
  def makeReporterThunk(source: String, agent: Agent, owner: JobOwner): ReporterLogoThunk =
    if(source.trim.isEmpty) throw new IllegalStateException("empty reporter source")
    else {
      val proc = invokeCompiler(source, Some(owner.displayName), false, agent.kind)
      new MyLogoThunk(source, agent, owner, false, proc) with ReporterLogoThunk {
        @throws(classOf[LogoException])
        def call(): Try[AnyRef] = {
          // This really ought to create a job and submit it through the job manager, instead of just
          // calling the procedure directly.  This is temporary code that never got cleaned up.
          // Submitting jobs through the job manager is supposed to be the only way that NetLogo code
          // is ever run. - ST 1/8/10
          val job = new ExclusiveJob(owner, agentset, procedure, 0, null, workspace, owner.random)
          val context = new Context(job, agent, 0, null, workspace)
          Try(context.callReporterProcedure(new Activation(procedure, null, 0)))
        }
      }
    }

  @throws(classOf[CompilerException])
  def makeCommandThunk(source: String, agent: Agent, owner: JobOwner): CommandLogoThunk =
    if(source.trim.isEmpty)
      new CommandLogoThunk { def call() = Try(false) }
    else {
      val fullSource = source + "\n__thunk-did-finish"
      val proc = invokeCompiler(fullSource, Some(owner.displayName), true, agent.kind)
      new MyLogoThunk(fullSource, agent, owner, true, proc) with CommandLogoThunk {
        @throws(classOf[LogoException])
        def call(): Try[Boolean] = ProcedureRunner.run(procedure, Some(owner))
      }
    }

  @throws(classOf[CompilerException])
  private class MyLogoThunk(source: String, agent: Agent, owner: JobOwner, command: Boolean, val procedure: Procedure) {
    val agentset = new ArrayAgentSet(agent.kind, 1, false)
    agentset.add(agent)
    procedure.topLevel = false
  }

  ///

  @throws(classOf[CompilerException])
  def invokeCompilerForRun(source: String, agentClass: AgentKind,
    callingProcedure: Procedure, reporter: Boolean): Procedure = {

    val vars =
      if (callingProcedure == null) Vector[String]()
      else callingProcedure.args
    val agentTypeHint = Evaluator.agentTypeHint(agentClass)

    val wrappedSource = if(reporter)
      // we put parens around what comes after "report", because we want to make
      // sure we don't let a malformed reporter like "3 die" past the compiler, since
      // "to-report foo report 3 die end" is syntactically valid but
      // "to-report foo report (3 die) end" isn't. - ST 11/12/09
      "to-report __runresult " +
        vars.mkString("[", " ", "]") + " " +
        agentTypeHint + " report ( " + source + " \n) __done end"
    else
      "to __run " + vars.mkString("[", " ", "]") + " " + agentTypeHint + " " + source + "\nend"
    val results = workspace.compiler.compileMoreCode(
      wrappedSource,
      Some(if(reporter) "runresult" else "run"),
      workspace.world.program, workspace.getProcedures,
      workspace.getExtensionManager, workspace.getCompilationEnvironment)
    results.head.init(workspace)
    results.head
  }


  @throws(classOf[CompilerException])
  private def invokeCompiler(source: String, displayName: Option[String], commands: Boolean, agentClass: AgentKind) = {
    val wrappedSource = Evaluator.getHeader(agentClass, commands) + source + Evaluator.getFooter(commands)
    val results =
      workspace.compiler.compileMoreCode(wrappedSource, displayName, workspace.world.program,
        workspace.getProcedures, workspace.getExtensionManager, workspace.getCompilationEnvironment)
    results.head.init(workspace)
    results.head
  }

  @throws(classOf[CompilerException])
  def readFromString(string: String) =
    workspace.compiler.readFromString(string, workspace.world, workspace.getExtensionManager)
}


object Evaluator {

  val agentTypeHint = Map[AgentKind, String](
    AgentKind.Observer -> "__observercode",
    AgentKind.Turtle -> "__turtlecode",
    AgentKind.Patch -> "__patchcode",
    AgentKind.Link -> "__linkcode")

  def getHeader(agentClass: AgentKind, commands: Boolean) = {
    val hint = agentTypeHint(agentClass)
    if(commands) "to __evaluator [] " + hint + " "
    else
      // we put parens around what comes after "report", because we want to make
      // sure we don't let a malformed reporter like "3 die" past the compiler, since
      // "to-report foo report 3 die end" is syntactically valid but
      // "to-report foo report (3 die) end" isn't. - ST 11/12/09
      "to-report __evaluator [] " + hint + " report ( "
  }

  def getFooter(commands: Boolean) =
    if(commands) "\n__done end" else "\n) __done end"

  def sourceOffset(agentClass: AgentKind, commands: Boolean): Int =
    getHeader(agentClass, commands).length
}
