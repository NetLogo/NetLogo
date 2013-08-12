// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang

import org.scalatest, scalatest.Assertions
import org.nlogo.{ api, agent }
import api.CompilerException.{RuntimeErrorAtCompileTimePrefix => runtimePrefix}
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
    finally fixture.workspace.dispose()
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
    new agent.ImporterJ.ErrorHandler() {
      def showError(title: String, errorDetails: String, fatalError: Boolean): Boolean =
        sys.error(s"$title / $errorDetails  / $fatalError")
    }

  // to get the test name into the stack traces on JobThread - ST 1/26/11, 8/7/13
  def owner(kind: api.AgentKind = api.AgentKind.Observer) =
    new api.SimpleJobOwner(name, workspace.world.mainRNG, kind)

  val compiler: CompilerInterface =
    Femto.scalaSingleton("org.nlogo.compile.Compiler")

  def runEntry(mode: TestMode, entry: Entry) {
    entry match {
      case Open(path) =>
        workspace.open(path)
      case Declaration(content) =>
        declare(content)
      case Command(kind, command, Success(_)) =>
        testCommand(command, kind, mode)
      case Command(kind, command, RuntimeError(message)) =>
        testCommand(command, kind, mode, error = Some(message))
      case Command(kind, command, StackTrace(trace)) =>
        testCommand(command, kind, mode, trace = Some(trace))
      case Command(kind, command, CompileError(message)) =>
        testCommand(command, kind, mode, compileError = Some(message))
      case Reporter(reporter, Success(result)) =>
        testReporter(reporter, mode, result = Some(result))
      case Reporter(reporter, RuntimeError(message)) =>
        testReporter(reporter, mode, error = Some(message))
      case Reporter(reporter, StackTrace(trace)) =>
        testReporter(reporter, mode, trace = Some(trace))
    }
  }

  def declare(source: String) {
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

  def testReporter(reporter: String, result: String) {
    testReporter(reporter, result = Some(result))
  }

  def testReporter(reporter: String,
                   mode: TestMode = NormalMode,
                   result: Option[String] = None,
                   error: Option[String] = None,
                   trace: Option[String] = None) {
    try {
      workspace.clearLastLogoException()
      val wrappedReporter = mode match {
        case NormalMode => reporter
        case RunMode    => s"""runresult "${api.StringUtils.escapeString(reporter)}""""
      }
      val compiled = workspace.compileReporter(wrappedReporter)
      val actualResult = workspace.runCompiledReporter(owner(), compiled)
      if(workspace.lastLogoException != null)
        throw workspace.lastLogoException
      if (error.isDefined || trace.isDefined)
        fail(s"""failed to cause runtime error: "$reporter"""")
      // To be as safe as we can, let's do two separate checks here...  we'll compare the results both
      // as Logo values, and as printed representations.  Most of the time these checks will come out
      // the same, but it's good to have a both, partially as a way of giving both
      // Utils.recursivelyEqual() and Dump.logoObject() lots of testing! - ST 5/8/03
      withClue(s"""$mode: not equals(): reporter "$reporter"""") {
        assertResult(result.get)(
          api.Dump.logoObject(actualResult, true, false))
      }
      assert(api.Equality.equals(actualResult,
        compiler.readFromString(result.get)),
        s"""$mode: not recursivelyEqual(): reporter "$reporter"""")
    }
    catch catcher(s"$mode: reporter: $reporter", error = error, trace = trace)
  }

  def testCommand(command: String,
                  kind: api.AgentKind = api.AgentKind.Observer,
                  mode: TestMode = NormalMode,
                  compileError: Option[String] = None,
                  error: Option[String] = None,
                  trace: Option[String] = None) {
    try {
      workspace.clearLastLogoException()
      val wrappedCommand = mode match {
        case NormalMode => command
        case RunMode    => s"""run "${api.StringUtils.escapeString(command)}""""
      }
      val compiled = workspace.compileCommands(wrappedCommand, kind)
      if (mode == NormalMode && compileError.isDefined)
        fail("no CompilerException occurred")
      workspace.runCompiledCommands(owner(kind), compiled)
      if(workspace.lastLogoException != null)
        throw workspace.lastLogoException
      if (error.isDefined || trace.isDefined)
        fail(s"""failed to cause runtime error: "$command"""")
    }
    catch catcher(s"command: $command", compileError, error, trace)
  }

  // ConstantFolder makes this complicated, by turning some runtime errors into
  // compile-time errors.  Furthermore in RunMode the compile-time error again
  // becomes a runtime error, but with "Runtime error: " tacked onto the front.
  private def catcher(clue: String,
                      compileError: Option[String] = None,
                      error: Option[String] = None,
                      trace: Option[String] = None)
      : PartialFunction[Throwable, Unit] = {
    case ex: api.LogoException if compileError.isDefined || error.isDefined || trace.isDefined =>
      withClue(clue) {
        for (expected <- compileError)
          checkMessage(ex, expected)
        for (expected <- error)
          checkMessage(ex, expected)
        for (expected <- trace)
          assertResult(expected)(workspace.lastErrorReport.stackTrace.get)
      }
    case ex: api.CompilerException if compileError.isDefined || error.isDefined =>
      withClue(clue) {
        for (expected <- compileError)
          checkMessage(ex, expected)
        for (expected <- error)
          checkMessage(ex, expected)
      }
  }
  private def checkMessage(ex: Exception, expected: String) =
    assertResult(expected)(
      ex.getMessage.stripPrefix(runtimePrefix))

}
