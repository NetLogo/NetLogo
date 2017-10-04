// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.core.{ AgentKind, CompilationEnvironment, CompilerException, Let }
import org.nlogo.api.{ JobOwner, LogoException, ReporterLogoThunk, CommandLogoThunk}
import org.nlogo.agent.{ Agent, AgentSet, World }
import org.nlogo.nvm.{ Activation, Context, JobManagerInterface, PresentationCompilerInterface, Procedure }

import scala.collection.immutable.Vector
import scala.util.Try

class Evaluator(jobManager: JobManagerInterface,
  compiler: PresentationCompilerInterface,
  world: World) {

import Evaluator.Linker

  @throws(classOf[CompilerException])
  def evaluateCommands(owner: JobOwner,
                       source: String,
                       agentSet: AgentSet = world.observers,
                       waitForCompletion: Boolean = true)(
                         implicit extensionManager: ExtensionManager,
                         compilationEnvironment: CompilationEnvironment,
                         procedures: Procedure.ProceduresMap,
                         linker: Linker) = {
    val procedure = invokeCompiler(source, None, true, agentSet.kind)
    jobManager.addJob(jobManager.makeConcurrentJob(owner, agentSet, procedure), waitForCompletion)
  }

  @throws(classOf[CompilerException])
  def evaluateReporter(owner: JobOwner, source: String, agents: AgentSet = world.observers)(
    implicit extensionManager: ExtensionManager,
    compilationEnvironment: CompilationEnvironment,
    procedures: Procedure.ProceduresMap,
    linker: Linker): Object = {
    val procedure = invokeCompiler(source, None, false, agents.kind)
    jobManager.addReporterJobAndWait(owner, agents, procedure)
  }

  @throws(classOf[CompilerException])
  def compileCommands(source: String, agentClass: AgentKind)(
    implicit extensionManager: ExtensionManager,
    compilationEnvironment: CompilationEnvironment,
    procedures: Procedure.ProceduresMap,
    linker: Linker): Procedure =
    invokeCompiler(source, None, true, agentClass)

  @throws(classOf[CompilerException])
  def compileReporter(source: String)(
    implicit extensionManager: ExtensionManager,
    compilationEnvironment: CompilationEnvironment,
    procedures: Procedure.ProceduresMap,
    linker: Linker): Procedure =
    invokeCompiler(source, None, false, AgentKind.Observer)

  /**
   * @return whether the code did a "stop" at the top level
   */
  def runCompiledCommands(owner: JobOwner, procedure: Procedure) = {
    val job = jobManager.makeConcurrentJob(owner, world.observers, procedure)
    jobManager.addJob(job, true)
    job.stopping
  }

  def runCompiledReporter(owner: JobOwner, procedure: Procedure) =
    jobManager.addReporterJobAndWait(owner, world.observers, procedure)

  @throws(classOf[CompilerException])
  def compileForRun(source: String, context: Context, reporter: Boolean)(
    implicit extensionManager: ExtensionManager,
    compilationEnvironment: CompilationEnvironment,
    procedures: Procedure.ProceduresMap,
    linker: Linker) =
    invokeCompilerForRun(source, context.agent.kind, context.activation.procedure, reporter)

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
      val let = new Let(p.name + "_finished")
      newActivation.binding.let(let, Boolean.box(false))
      context.activation = newActivation
      context.job.random = ownerOption.map(_.random).getOrElse(world.mainRNG.clone)
      val procedureResult = Try {
        context.runExclusiveJob(world.observers, 0)
        ! (newActivation.binding.getLet(let) == Boolean.box(true))
      }
      context.activation = oldActivation
      context.job.random = oldRandom
      procedureResult
    }
  }

  @throws(classOf[CompilerException])
  def makeReporterThunk(source: String, agent: Agent, owner: JobOwner)(
    implicit extensionManager: ExtensionManager,
    compilationEnvironment: CompilationEnvironment,
    procedures: Procedure.ProceduresMap,
    linker: Linker): ReporterLogoThunk = {
    if(source.trim.isEmpty) throw new IllegalStateException("empty reporter source")
    else {
      val proc = invokeCompiler(source, Some(owner.displayName), false, agent.kind)
      new MyLogoThunk(source, agent, owner, false, proc) with ReporterLogoThunk {
        @throws(classOf[LogoException])
        def call(): Try[AnyRef] = {
          Try(jobManager.callReporterProcedure(owner, agentset, procedure))
        }
      }
    }
  }

  @throws(classOf[CompilerException])
  def makeCommandThunk(source: String, agent: Agent, owner: JobOwner)(
    implicit extensionManager: ExtensionManager,
    compilationEnvironment: CompilationEnvironment,
    procedures: Procedure.ProceduresMap,
    linker: Linker): CommandLogoThunk = {
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
  }

  @throws(classOf[CompilerException])
  private class MyLogoThunk(source: String, agent: Agent, owner: JobOwner, command: Boolean, val procedure: Procedure) {
    val agentset = AgentSet.fromAgent(agent)
    procedure.topLevel = false
  }

  @throws(classOf[CompilerException])
  def invokeCompilerForRun(source: String, agentClass: AgentKind, callingProcedure: Procedure, reporter: Boolean)(
    implicit extensionManager: ExtensionManager,
    compilationEnvironment: CompilationEnvironment,
    procedures: Procedure.ProceduresMap,
    linker: Linker): Procedure = {

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
    val results = compiler.compileMoreCode(
      wrappedSource,
      Some(if(reporter) "runresult" else "run"),
      world.program, procedures, extensionManager, compilationEnvironment)
    linker.link(results.head)
  }


  @throws(classOf[CompilerException])
  private def invokeCompiler(source: String, displayName: Option[String], commands: Boolean, agentClass: AgentKind)(
    implicit extensionManager: ExtensionManager,
    compilationEnvironment: CompilationEnvironment,
    procedures: Procedure.ProceduresMap,
    linker: Linker) = {
    val wrappedSource = Evaluator.getHeader(agentClass, commands) + source + Evaluator.getFooter(commands)
    val results =
      compiler.compileMoreCode(wrappedSource, displayName, world.program, procedures, extensionManager, compilationEnvironment)
    linker.link(results.head)
  }

  @throws(classOf[CompilerException])
  def readFromString(string: String, extensionManager: ExtensionManager) =
    compiler.readFromString(string, world, extensionManager)
}


object Evaluator {

  trait Linker {
    def link(p: Procedure): Procedure
  }

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
