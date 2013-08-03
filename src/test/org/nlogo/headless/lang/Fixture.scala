// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang

import org.scalatest, scalatest.Assertions
import org.nlogo.api
import org.nlogo.nvm.CompilerInterface
import org.nlogo.util.Femto

trait FixtureSuite extends scalatest.fixture.FunSuite {
  type FixtureParam = Fixture
  override def withFixture(test: OneArgTest) =
    Fixture.withFixture(test.name) { fixture =>
      withFixture(test.toNoArgTest(fixture))
    }
}

object Fixture {
  def withFixture[T](name: String)(fn: Fixture => T) = {
    val fixture = new Fixture(name)
    try fn(fixture)
    finally fixture.dispose()
  }
}

class Fixture(name: String) {

  import Assertions._

  // many individual tests expect this to exist - ST 7/31/13
  new java.io.File("tmp").mkdir()

  val workspace = HeadlessWorkspace.newInstance
  workspace.silent = true
  workspace.initForTesting(
    new api.WorldDimensions(-5, 5, -5, 5),
    HeadlessWorkspace.TestDeclarations)
  // the default error handler just spits something to stdout or stderr or somewhere.
  // we want to fail hard. - ST 7/21/10
  workspace.importerErrorHandler =
    new org.nlogo.agent.ImporterJ.ErrorHandler() {
      def showError(title: String, errorDetails: String, fatalError: Boolean): Boolean =
        sys.error(title + " / " + errorDetails + " / " + fatalError)
    }

  def dispose() { workspace.dispose() }

  // to get the test name into the stack traces on JobThread - ST 1/26/11, 8/7/13
  val owner =
    new api.SimpleJobOwner(name, workspace.world.mainRNG)

  val compiler: CompilerInterface =
    Femto.scalaSingleton("org.nlogo.compile.Compiler")

  def runEntry(mode: TestMode, entry: Entry) {
    entry match {
      case Open(modelPath) =>
        open(modelPath)
      case Procedure(content) =>
        defineProcedures(content)
      case Command(kind, command, Success(_)) =>
        testCommand(command, kind, mode)
      case Command(kind, command, RuntimeError(message)) =>
        testCommandError(command, message, kind, mode)
      case Command(kind, command, CompileError(message)) =>
        testCommandCompilerErrorMessage(command, message, kind)
      case Command(kind, command, StackTrace(message)) =>
        testCommandErrorStackTrace(command, message, kind, mode)
      case Reporter(reporter, Success(message)) =>
        testReporter(reporter, message, mode)
      case Reporter(reporter, RuntimeError(message)) =>
        testReporterError(reporter, message, mode)
      case Reporter(reporter, StackTrace(message)) =>
        testReporterErrorStackTrace(reporter, message, mode)
    }
  }

  def open(path: String) {
    workspace.open(path)
  }

  def defineProcedures(source: String) {
    val results = {
      compiler.compileProgram(
        HeadlessWorkspace.TestDeclarations + source,
        api.Program.empty,
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
    assert(api.Equality.equals(actualResult,
      compiler.readFromString(expectedResult)),
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
        import api.CompilerException.{RuntimeErrorAtCompileTimePrefix => prefix}
        if(ex.getMessage.startsWith(prefix))
          assertResult(prefix + expectedError)(
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
                  kind: api.AgentKind = api.AgentKind.Observer,
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
                       kind: api.AgentKind = api.AgentKind.Observer,
                       mode: TestMode = NormalMode) {
    try {
      testCommand(command, kind, mode)
      fail("failed to cause runtime error: \"" + command + "\"")
    }
    catch {
      case ex: api.LogoException =>
        withClue(mode + ": command: " + command) {
          assertResult(error)(ex.getMessage)
        }
    }
  }
  def testCommandErrorStackTrace(command: String, stackTrace: String,
                       kind: api.AgentKind = api.AgentKind.Observer,
                       mode: TestMode = NormalMode) {
    try {
      testCommand(command, kind, mode)
      fail("failed to cause runtime error: \"" + command + "\"")
    }
    catch {
      case ex: api.LogoException =>
        withClue(mode + ": command: " + command) {
          assertResult(stackTrace)(workspace.lastErrorReport.stackTrace.get)
        }
    }
  }
  def testCommandCompilerErrorMessage(command: String, errorMessage: String,
                                      kind: api.AgentKind = api.AgentKind.Observer)
  {
    try {
      workspace.compileCommands(command, kind)
      fail("no CompilerException occurred")
    }
    catch {
      case ex: api.CompilerException =>
        assertResult(errorMessage)(ex.getMessage)
    }
  }

}
