// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.core.{ AgentKind, SourceWrapping }
import org.nlogo.api.{JobOwner, ReporterLogoThunk, CommandLogoThunk}
import org.nlogo.agent.{Agent, AgentSet}
import org.nlogo.nvm.{ ExclusiveJob, Activation, CompilerFlags,
                       Context, ImportHandler, Procedure, Reporter }

import scala.util.Try

class Evaluator(workspace: AbstractWorkspace) {

  def evaluateCommands(owner: JobOwner,
                       source: String,
                       agentSet: AgentSet = workspace.world.observers,
                       waitForCompletion: Boolean = true,
                       flags: CompilerFlags = workspace.flags) {
    val procedure = invokeCompiler(source, None, true, agentSet.kind, flags)
    workspace.jobManager.addJob(
      workspace.jobManager.makeConcurrentJob(owner, agentSet, workspace, procedure),
      waitForCompletion)
  }

  def evaluateReporter(owner: JobOwner, source: String,
      agents: AgentSet = workspace.world.observers,
      flags: CompilerFlags = workspace.flags): Object = {
    val procedure = invokeCompiler(source, None, false, agents.kind, flags)
    workspace.jobManager.addReporterJobAndWait(owner, agents, workspace, procedure)
  }

  def compileCommands(source: String, kind: AgentKind = AgentKind.Observer,
      flags: CompilerFlags = workspace.flags): Procedure =
    invokeCompiler(source, None, true, kind, flags)

  def compileReporter(source: String, flags: CompilerFlags = workspace.flags) =
    invokeCompiler(source, None, false, AgentKind.Observer, flags)

  /**
   * @return whether the code did a "stop" at the top level
   */
  def runCompiledCommands(owner: JobOwner, procedure: Procedure) = {
    val job = workspace.jobManager.makeConcurrentJob(
      owner, workspace.world.agentSetOfKind(owner.kind), workspace, procedure)
    workspace.jobManager.addJob(job, true)
    job.stopping
  }

  def runCompiledReporter(owner: JobOwner, procedure: Procedure) =
    workspace.jobManager.addReporterJobAndWait(owner,
      workspace.world.agentSetOfKind(owner.kind), workspace, procedure)

  ///

  def compileForRun(source: String, context: Context,reporter: Boolean) =
    invokeCompilerForRun(source, context.agent.kind, context.activation.procedure, reporter)

  ///

  def withContext(context: Context)(f: => Unit) {
    val oldContext = ProcedureRunner.context
    ProcedureRunner.context = context
    try f
    finally ProcedureRunner.context = oldContext
  }

  object ProcedureRunner {
    private[Evaluator] var context: Context = null
    def hasContext = context != null
    def report(reporter: Reporter, a: Agent = workspace.world.observer) =
      context.evaluateReporter(a, reporter)
    def run(p: Procedure): Try[Boolean] = {
      val oldActivation = context.activation
      val newActivation = new Activation(p, context.activation, 1)
      val oldRandom = context.job.random
      context.activation = newActivation
      context.job.random = workspace.world.mainRNG.clone
      val procedureResult = Try {
        context.runExclusiveJob(workspace.world.observers, 0)
        !workspace.completedActivations.getOrElse(newActivation, false)
      }
      context.activation = oldActivation
      context.job.random = oldRandom
      procedureResult
    }
  }

  // At present (October 2012) this is only used in the slider constraint code, which has been known
  // since we wrote it not to handle threading correctly.  It hasn't been fixed because the feature
  // (reporters as slider bounds) is relatively little known and little used.  This code might
  // actually be correct if called from the job thread, but actually, the current slider code
  // doesn't do that, it calls it from the event thread.  The event thread should not be running
  // NetLogo code; submitting jobs through the job manager is supposed to be the only way that
  // NetLogo code is ever run.  Fixing it wouldn't be easy because the event thread is not allowed
  // to block waiting for the job thread to finish something, since this may cause deadlock.  (And
  // not just in theory either; it definitely deadlocks in practice.)  So, consider this code
  // to have a big "beware" sign on it -- it shouldn't be called from the event thread, but it
  // isn't actually known whether it works when called from the job thread, either.
  //
  // Note that this code is very similar to the ProcedureRunner code above, which is used by
  // plotting.  That code, I believe to be correct and to handle threading correctly.  But the
  // snippets of code in plots are always commands, never reporters, so the plotting stuff never
  // needs makeReporterThunk. - ST 10/9/12
  def makeReporterThunk(source: String, agent: Agent, owner: JobOwner): ReporterLogoThunk =
    if(source.trim.isEmpty) throw new IllegalStateException("empty reporter source")
    else {
      val proc = invokeCompiler(source, Some(owner.displayName), false, agent.kind)
      new MyLogoThunk(source, agent, owner, false, proc) with ReporterLogoThunk {
        def call(): Try[AnyRef] = {
          // This really ought to create a job and submit it through the job manager, instead of just
          // calling the procedure directly.  This is temporary code that never got cleaned up.
          // Submitting jobs through the job manager is supposed to be the only way that NetLogo code
          // is ever run. - ST 1/8/10
          val job = new ExclusiveJob(owner, agentset, procedure, 0, null, workspace, owner.random, ExclusiveJob.initialComeUpForAir)
          val context = new Context(job, agent, 0, null)
          Try(context.callReporterProcedure(new Activation(procedure, null, 0)))
        }
      }
    }

  def makeCommandThunk(source: String, agent: Agent, owner: JobOwner): CommandLogoThunk =
    if(source.trim.isEmpty)
      new CommandLogoThunk { def call() = Try(false) }
    else {
      val fullSource = source + "\n__thunk-did-finish"
      val proc = invokeCompiler(fullSource, Some(owner.displayName), true, agent.kind)
      new MyLogoThunk(fullSource, agent, owner, true, proc) with CommandLogoThunk {
        def call(): Try[Boolean] = ProcedureRunner.run(procedure)
      }
    }

  private class MyLogoThunk(source: String, agent: Agent, owner: JobOwner, command: Boolean, val procedure: Procedure) {
    val agentset = AgentSet.fromAgent(agent)
    procedure.topLevel = false
  }

  ///

  def invokeCompilerForRun(source: String, kind: AgentKind,
    callingProcedure: Procedure, reporter: Boolean): Procedure = {

    val vars =
      if (callingProcedure == null) Vector()
      else callingProcedure.args
    val agentKindHint = SourceWrapping.agentKindHint(kind)

    val wrappedSource = if(reporter)
      // we put parens around what comes after "report", because we want to make
      // sure we don't let a malformed reporter like "3 die" past the compiler, since
      // "to-report foo report 3 die end" is syntactically valid but
      // "to-report foo report (3 die) end" isn't. - ST 11/12/09
      "to-report __runresult " +
        vars.mkString("[", " ", "]") + " " +
        agentKindHint + " report ( " + source + " \n) __done end"
    else
      "to __run " + vars.mkString("[", " ", "]") + " " + agentKindHint + " " + source + "\nend"
    val results = workspace.compiler.compileMoreCode(
      wrappedSource,
      Some(if(reporter) "runresult" else "run"),
      workspace.world.program, workspace.procedures,
      workspace.getExtensionManager,
      workspace.compilationEnvironment,
      workspace.flags)
    results.head.init(workspace)
    results.head
  }


  private def invokeCompiler(source: String, displayName: Option[String], commands: Boolean, kind: AgentKind, flags: CompilerFlags = workspace.flags) = {
    val wrappedSource = SourceWrapping.getHeader(kind, commands) + source + SourceWrapping.getFooter(commands)
    val results =
      workspace.compiler.compileMoreCode(wrappedSource, displayName, workspace.world.program,
        workspace.procedures, workspace.getExtensionManager, workspace.compilationEnvironment, flags)
    results.head.init(workspace)
    results.head
  }

  def readFromString(string: String) =
    workspace.compiler.utilities.readFromString(
      string, new ImportHandler(workspace.world, workspace.getExtensionManager))
}
