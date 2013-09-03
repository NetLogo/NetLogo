// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.scalatest.Assertions
import org.nlogo.agent.{ Agent, Observer }
import org.nlogo.api.{ AgentKind, Equality, CompilerException, JobOwner, LogoException, Program, Version, WorldDimensions, WorldDimensions3D }
import org.nlogo.nvm.CompilerInterface
import org.nlogo.util.Femto

object AbstractTestLanguage {
  sealed abstract class TestMode
  case object NormalMode extends TestMode
  case object RunMode extends TestMode
}

trait AbstractTestLanguage extends Assertions {

  import AbstractTestLanguage._

  val compiler = Femto.scalaSingleton(classOf[CompilerInterface],
                                      "org.nlogo.compile.Compiler")
  var workspace: HeadlessWorkspace = _

  def owner: JobOwner = workspace.defaultOwner

  def init() {
    workspace = HeadlessWorkspace.newInstance
    workspace.silent = true
    workspace.initForTesting(
      if(Version.is3D)
        new WorldDimensions3D(-5, 5, -5, 5, -5, 5)
      else
        new WorldDimensions(-5, 5, -5, 5),
      HeadlessWorkspace.TestDeclarations)
  }

  def defineProcedures(source: String) {
    val results = {
      import collection.JavaConverters._
      compiler.compileProgram(
        HeadlessWorkspace.TestDeclarations + source,
        Program.empty(Version.is3D),
        workspace.getExtensionManager)
    }
    workspace.procedures = results.proceduresMap
    workspace.world.program(results.program)
    workspace.init()
    workspace.world.realloc()
  }

  def testReporter(reporter: String, expectedResult: String, mode: TestMode = NormalMode) {
    workspace.clearLastLogoException()
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
                           compiler.readFromString(expectedResult, workspace.world.program.is3D)),
           mode + ": not recursivelyEqual(): reporter \"" + reporter + "\"")
  }
  private def privateTestReporterError(reporter: String,
                                       expectedError: String,
                                       actualError: => String,
                                       mode: TestMode) {
    try {
      testReporter(reporter, expectedError, mode)
      fail("failed to cause runtime error: \"" + reporter + "\"")
    }
    catch {
      case ex: Exception =>
        // PureConstantOptimizer turns some errors that would be runtime errors into compile-time
        // errors, so we have to check for those
        if(ex.getMessage.startsWith(CompilerException.RuntimeErrorAtCompileTimePrefix))
          assertResult(CompilerException.RuntimeErrorAtCompileTimePrefix + expectedError)(
            ex.getMessage)
        else
          withClue(mode + ": reporter: " + reporter) {
            assertResult(expectedError)(actualError)
          }
    }
  }
  def testReporterError(reporter: String, error: String, mode: TestMode) {
    privateTestReporterError(reporter, error, workspace.lastLogoException.getMessage, mode)
  }
  def testReporterErrorStackTrace(reporter: String, stackTrace: String, mode: TestMode) {
    privateTestReporterError(reporter, stackTrace, workspace.lastErrorReport.stackTrace.get, mode)
  }
  def testCommand(command: String,
                  kind: AgentKind = AgentKind.Observer,
                  mode: TestMode = NormalMode) {
    workspace.clearLastLogoException()
    workspace.evaluateCommands(owner,
      if(mode == NormalMode) command
      else ("run \"" + org.nlogo.api.StringUtils.escapeString(command) + "\""),
      workspace.world.kindToAgentSet(kind), true)
    if(workspace.lastLogoException != null)
      throw workspace.lastLogoException
  }
  def testCommandError(command: String, error: String,
                       kind: AgentKind = AgentKind.Observer,
                       mode: TestMode = NormalMode) {
    try {
      testCommand(command, kind, mode)
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
                       kind: AgentKind = AgentKind.Observer,
                       mode: TestMode = NormalMode) {
    try {
      testCommand(command, kind, mode)
      fail("failed to cause runtime error: \"" + command + "\"")
    }
    catch {
      case ex: LogoException =>
        withClue(mode + ": command: " + command) {
          assertResult(stackTrace)(workspace.lastErrorReport.stackTrace.get)
        }
    }
  }
  def testCommandCompilerErrorMessage(command: String, errorMessage: String,
                                      kind: AgentKind = AgentKind.Observer)
  {
    try {
      workspace.compileCommands(command, kind)
      fail("no CompilerException occurred")
    }
    catch {
      case ex: CompilerException =>
        assertResult(errorMessage)(ex.getMessage)
    }
  }
}
