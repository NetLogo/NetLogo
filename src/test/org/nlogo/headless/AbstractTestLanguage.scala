package org.nlogo.headless

import org.scalatest.Assertions
import org.nlogo.agent.{Agent, Observer}
import org.nlogo.api.{Equality, CompilerException, JobOwner, LogoException, Version, WorldDimensions, WorldDimensions3D}
import org.nlogo.nvm.CompilerInterface
import org.nlogo.util.Femto

object AbstractTestLanguage {
  sealed abstract class TestMode
  case object NormalMode extends TestMode
  case object RunMode extends TestMode
}

abstract class AbstractTestLanguage extends Assertions {

  import AbstractTestLanguage._

  val compiler = Femto.scalaSingleton(classOf[CompilerInterface],
                                      "org.nlogo.compiler.Compiler")
  var workspace: HeadlessWorkspace = _

  def owner: JobOwner = workspace.defaultOwner

  def init() {
    workspace = HeadlessWorkspace.newInstance
    workspace.silent(true)
    workspace.initForTesting(
      if(Version.is3D)
        new WorldDimensions3D(-5, 5, -5, 5, -5, 5)
      else
        new WorldDimensions(-5, 5, -5, 5),
      HeadlessWorkspace.TEST_DECLARATIONS)
    workspace.getHubNetManager.loadClientInterface(Nil, Version.version)
  }

  def defineProcedures(source: String) {
    import org.nlogo.util.JCL._
    val results = compiler.compileProgram(
      HeadlessWorkspace.TEST_DECLARATIONS + source,
      workspace.world.newProgram(Nil),
      workspace.getExtensionManager()) 
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
      expect(expectedResult)(
        org.nlogo.api.Dump.logoObject(actualResult, true, false))
    }
    assert(Equality.equals(actualResult,
                           compiler.readFromString(expectedResult, workspace.world().program.is3D)),
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
      case ex =>
        // PureConstantOptimizer turns some errors that would be runtime errors into compile-time
        // errors, so we have to check for those
        if(ex.getMessage.startsWith(CompilerException.RUNTIME_ERROR_AT_COMPILE_TIME_MSG_PREFIX))
          expect(CompilerException.RUNTIME_ERROR_AT_COMPILE_TIME_MSG_PREFIX + expectedError)(
            ex.getMessage)
        else
          withClue(mode + ": reporter: " + reporter) {
            expect(expectedError)(actualError)
          }
    }
  }
  def testReporterError(reporter: String, error: String, mode: TestMode) {
    privateTestReporterError(reporter, error, workspace.lastLogoException.getMessage, mode)
  }
  def testReporterErrorStackTrace(reporter: String, stackTrace: String, mode: TestMode) {
    privateTestReporterError(reporter, stackTrace, workspace.lastErrorReport.getNetLogoStackTrace.get, mode)
  }
  def testCommand(command: String,
                  agentClass: Class[_ <: Agent] = classOf[Observer],
                  mode: TestMode = NormalMode) {
    workspace.lastLogoException = null
    workspace.evaluateCommands(owner,
      if(mode == NormalMode) command
      else ("run \"" + org.nlogo.api.StringUtils.escapeString(command) + "\""),
      workspace.world.agentClassToAgentSet(agentClass), true) 
    if(workspace.lastLogoException != null)
      throw workspace.lastLogoException 
  }
  def testCommandError(command: String, error: String,
                       agentClass: Class[_ <: Agent] = classOf[Observer],
                       mode: TestMode = NormalMode) {
    try {
      testCommand(command, agentClass, mode) 
      fail("failed to cause runtime error: \"" + command + "\"") 
    }
    catch {
      case ex: LogoException =>
        withClue(mode + ": command: " + command) {
          expect(error)(ex.getMessage)
        }
    }
  }
  def testCommandErrorStackTrace(command: String, stackTrace: String,
                       agentClass: Class[_ <: Agent] = classOf[Observer],
                       mode: TestMode = NormalMode) {
    try {
      testCommand(command, agentClass, mode)
      fail("failed to cause runtime error: \"" + command + "\"")
    }
    catch {
      case ex: LogoException =>
        withClue(mode + ": command: " + command) {
          expect(stackTrace)(workspace.lastErrorReport.getNetLogoStackTrace.get)
        }
    }
  }
  def testCommandCompilerErrorMessage(command: String, errorMessage: String,
                                      agentClass: Class[_ <: Agent] = classOf[Observer])
  {
    try {
      workspace.compileCommands(command, agentClass) 
      fail("no CompilerException occurred")
    }
    catch {
      case ex: CompilerException =>
        expect(errorMessage)(ex.getMessage)
    }
  }
}
