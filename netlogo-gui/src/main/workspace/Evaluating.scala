// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.agent.{ Agent, AgentSet }
import org.nlogo.core.{ AgentKind, CompilationEnvironment, CompilerException }
import org.nlogo.api.{ CommandLogoThunk, JobOwner, LogoException, MersenneTwisterFast, ReporterLogoThunk, SimpleJobOwner }
import org.nlogo.nvm.Procedure

trait Evaluating extends { this: AbstractWorkspace =>
  var lastLogoException: LogoException = null
  private implicit val implicitExtensionManager: ExtensionManager = getExtensionManager
  private implicit val implicitCompilationEnvironment: CompilationEnvironment = getCompilationEnvironment
  private implicit def implicitWorkspaceProcedures: Procedure.ProceduresMap = procedures
  private implicit val implicitLinker = linker

  def evaluator: Evaluator

  def clearLastLogoException() { lastLogoException = null }

  @throws(classOf[CompilerException])
  def makeReporterThunk(source: String, jobOwnerName: String): ReporterLogoThunk =
    evaluator.makeReporterThunk(source, world.observer,
      new SimpleJobOwner(jobOwnerName, auxRNG, AgentKind.Observer))
  @throws(classOf[CompilerException])
  def makeCommandThunk(source: String, jobOwnerName: String): CommandLogoThunk =
    makeCommandThunk(source, jobOwnerName, auxRNG)
  @throws(classOf[CompilerException])
  def makeCommandThunk(source: String, jobOwnerName: String, rng: MersenneTwisterFast): CommandLogoThunk =
    evaluator.makeCommandThunk(source, world.observer,
      new SimpleJobOwner(jobOwnerName, rng, AgentKind.Observer))
  @throws(classOf[CompilerException])
  def evaluateCommands(owner: JobOwner, source: String) = {
    evaluator.evaluateCommands(owner, source)
  }
  @throws(classOf[CompilerException])
  def evaluateCommands(owner: JobOwner, source: String, waitForCompletion: Boolean) = {
    evaluator.evaluateCommands(owner, source, world.observers, waitForCompletion)
  }
  @throws(classOf[CompilerException])
  def evaluateCommands(owner: JobOwner, source: String, agent: Agent, waitForCompletion: Boolean) =
    evaluator.evaluateCommands(owner, source, AgentSet.fromAgent(agent), waitForCompletion)
  @throws(classOf[CompilerException])
  def evaluateCommands(owner: JobOwner, source: String, agents: AgentSet, waitForCompletion: Boolean) =
      evaluator.evaluateCommands(owner, source, agents, waitForCompletion)
  @throws(classOf[CompilerException])
  def evaluateReporter(owner: JobOwner, source: String) =
    evaluator.evaluateReporter(owner, source, world.observers)
  @throws(classOf[CompilerException])
  def evaluateReporter(owner: JobOwner, source: String, agent: Agent) =
    evaluator.evaluateReporter(owner, source, AgentSet.fromAgent(agent))
  @throws(classOf[CompilerException])
  def evaluateReporter(owner: JobOwner, source: String, agents: AgentSet) =
    evaluator.evaluateReporter(owner, source, agents)
  @throws(classOf[CompilerException])
  def compileCommands(source: String): Procedure =
    compileCommands(source, AgentKind.Observer)
  @throws(classOf[CompilerException])
  def compileCommands(source: String, agentClass: AgentKind): Procedure =
    evaluator.compileCommands(source, agentClass)
  @throws(classOf[CompilerException])
  def compileReporter(source: String): Procedure =
    evaluator.compileReporter(source)
  def runCompiledCommands(owner: JobOwner, procedure: Procedure): Boolean =
    evaluator.runCompiledCommands(owner, procedure)
  def runCompiledReporter(owner: JobOwner, procedure: Procedure): AnyRef =
    evaluator.runCompiledReporter(owner, procedure)

  val defaultOwner =
    new SimpleJobOwner(getClass.getSimpleName, world.mainRNG, AgentKind.Observer)
  /**
   * Runs NetLogo commands and waits for them to complete.
   *
   * @param source The command or commands to run
   * @throws org.nlogo.core.CompilerException
   *                       if the code fails to compile
   * @throws org.nlogo.api.LogoException if the code fails to run
   */
  @throws(classOf[CompilerException])
  @throws(classOf[LogoException])
  def command(source: String): Unit = {
    evaluator.evaluateCommands(defaultOwner, source, world.observers, true)
    if (lastLogoException != null) {
      val ex = lastLogoException
      lastLogoException = null
      throw ex
    }
  }

  /**
   * Runs a NetLogo reporter.
   *
   * @param source The reporter to run
   * @return the result reported; may be of type java.lang.Integer, java.lang.Double,
   *         java.lang.Boolean, java.lang.String, {@link org.nlogo.core.LogoList},
   *         {@link org.nlogo.api.Agent}, AgentSet, or Nobody
   * @throws org.nlogo.core.CompilerException
   *                       if the code fails to compile
   * @throws org.nlogo.api.LogoException if the code fails to run
   */
  @throws(classOf[CompilerException])
  @throws(classOf[LogoException])
  def report(source: String): AnyRef = {
    val result = evaluator.evaluateReporter(defaultOwner, source, world.observers)
    if (lastLogoException != null) {
      val ex = lastLogoException
      lastLogoException = null
      throw ex
    }
    result
  }
}
