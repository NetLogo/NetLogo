// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.scalatest.Assertions
import org.nlogo.agent.Agent
import org.nlogo.api.{Equality, JobOwner, LogoException, NetLogoLegacyDialect, NetLogoThreeDDialect, Version, WorldDimensions3D}
import org.nlogo.core.{ AgentKind, CompilerException, Model, WorldDimensions }
import org.nlogo.nvm.CompilerInterface
import org.nlogo.core.Femto

object AbstractTestLanguage {
  sealed abstract class TestMode
  case object NormalMode extends TestMode
  case object RunMode extends TestMode
}

trait AbstractTestLanguage extends Assertions {

  import AbstractTestLanguage._

  val compiler = Femto.get[CompilerInterface]("org.nlogo.compiler.Compiler", if (Version.is3D) NetLogoThreeDDialect else NetLogoLegacyDialect)
  var workspace: HeadlessWorkspace = _

  def owner: JobOwner = workspace.defaultOwner

  def init() {
    workspace = HeadlessWorkspace.newInstance
    workspace.silent = true
    workspace.initForTesting(
      if(Version.is3D)
        new WorldDimensions3D(-5, 5, -5, 5, -5, 5)
      else
        new WorldDimensions(-5, 5, -5, 5), "")
  }

  def defineProcedures(source: String) {
    val results = {
      import collection.JavaConverters._
      compiler.compileProgram(
        HeadlessWorkspace.TestDeclarations + source,
        workspace.world.newProgram(Seq[String]()),
        workspace.getExtensionManager(), workspace.getCompilationEnvironment)
    }
    workspace.setProcedures(results.proceduresMap)
    workspace.world.program(results.program)
    workspace.init()
    workspace.world.realloc()
  }

  def openModel(model: Model): Unit = {
    val results = {
      import collection.JavaConverters._
      compiler.compileProgram(
        model.code,
        workspace.world.newProgram(Seq[String]()),
        workspace.getExtensionManager(), workspace.getCompilationEnvironment)
    }
    workspace.setProcedures(results.proceduresMap)
    workspace.world.program(results.program)
    workspace.init()
    workspace.world.realloc()
  }

  def testReporter(reporter: String, expectedResult: String, mode: TestMode = NormalMode) {
    workspace.lastLogoException = null
    val actualResult = workspace.evaluateReporter(owner,
      if(mode == NormalMode) reporter
      else ("runresult \"" + org.nlogo.api.StringUtils.escapeString(reporter) + "\""),
      workspace.world.observer())
    if(workspace.lastLogoException != null)
      throw workspace.lastLogoException
    // To be as safe as we can, let's do two separate checks here...  we'll compare the results both
    // as Logo values, and as printed representations.  Most of the time these checks will come out
    // the same, but it's good to have a both, partially as a way of giving both
    // Utils.recursivelyEqual() and Dump.logoObject() lots of testing! - ST 5/8/03
    withClue(mode + ": not equals(): reporter \"" + reporter + "\"") {
      assertResult(expectedResult)(
        org.nlogo.api.Dump.logoObject(actualResult, true, false))
    }
    assert(Equality.equals(actualResult,
                           compiler.readFromString(expectedResult)),
           mode + ": not recursivelyEqual(): reporter \"" + reporter + "\"")
  }
  private def privateTestReporterError(reporter: String,
                                       expectedError: String,
                                       actualError: => Option[String],
                                       mode: TestMode) {
    try {
      testReporter(reporter, expectedError, mode)
      fail("failed to cause runtime error: \"" + reporter + "\"")
    } catch {
      // PureConstantOptimizer throws would-be runtime errors at compile-time, so we check those
      case ex: CompilerException
        if (ex.getMessage.startsWith(CompilerException.RuntimeErrorAtCompileTimePrefix)) =>
          assertResult(CompilerException.RuntimeErrorAtCompileTimePrefix + expectedError)(ex.getMessage)
      case ex: CompilerException =>
        fail("expected runtime error, got compile error: " + ex.getMessage)
      case ex: Throwable =>
        withClue(mode + ": reporter: " + reporter) {
          if (actualError.isEmpty)
            fail("expected to find an error, but none found")
          else
            assertResult(expectedError)(actualError.get)
        }
    }
  }
  def testReporterError(reporter: String, error: String, mode: TestMode) {
    privateTestReporterError(reporter, error, Option(workspace.lastLogoException).map(_.getMessage), mode)
  }
  def testReporterErrorStackTrace(reporter: String, stackTrace: String, mode: TestMode) {
    privateTestReporterError(reporter, stackTrace, Option(workspace.lastErrorReport).flatMap(_.stackTrace), mode)
  }
  def testCommand(command: String,
                  agentClass: AgentKind = AgentKind.Observer,
                  mode: TestMode = NormalMode) {
    workspace.lastLogoException = null
    workspace.evaluateCommands(owner,
      if(mode == NormalMode) command
      else ("run \"" + org.nlogo.api.StringUtils.escapeString(command) + "\""),
      workspace.world.agentKindToAgentSet(agentClass), true)
    if(workspace.lastLogoException != null)
      throw workspace.lastLogoException
  }
  def testCommandError(command: String, error: String,
                       agentClass: AgentKind = AgentKind.Observer,
                       mode: TestMode = NormalMode) {
    try {
      testCommand(command, agentClass, mode)
      fail("failed to cause runtime error: \"" + command + "\"")
    }
    catch {
      case ex: LogoException =>
        withClue(mode + ": command: " + command) {
          assertResult(error)(ex.getMessage)
        }
    }
  }
  def testCommandErrorStackTrace(command: String, stackTrace: String,
                       agentClass: AgentKind = AgentKind.Observer,
                       mode: TestMode = NormalMode) {
    try {
      testCommand(command, agentClass, mode)
      fail("failed to cause runtime error: \"" + command + "\"")
    }
    catch {
      case ex: LogoException =>
        withClue(mode + ": command: " + command) {
          // println(workspace.lastErrorReport.stackTrace.get)
          assertResult(stackTrace)(workspace.lastErrorReport.stackTrace.get)
        }
    }
  }
  def testCommandCompilerErrorMessage(command: String, errorMessage: String,
                                      agentClass: AgentKind = AgentKind.Observer)
  {
    try {
      workspace.compileCommands(command, agentClass)
      fail("no CompilerException occurred")
    }
    catch {
      case ex: CompilerException =>
        assertResult(errorMessage)(ex.getMessage)
    }
  }

  def testCompilerError(model: Model, errorMessage: String): Unit = {
    try {
      openModel(model)
      fail("no CompilerException occurred")
    } catch {
      case ex: CompilerException =>
        assertResult(errorMessage)(ex.getMessage)
    }
  }
}
