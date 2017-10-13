// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.scalactic.source.Position
import org.scalatest.Tag

import org.nlogo.agent.CompilationManagement
import org.nlogo.api.{Equality, JobOwner, LogoException, NetLogoLegacyDialect,
  NetLogoThreeDDialect, TwoDVersion, ThreeDVersion, Version, WorldDimensions3D }
import org.nlogo.core.{ AgentKind, CompilerException, Model, Program, WorldDimensions }
import org.nlogo.nvm.{ CompilerFlags, PresentationCompilerInterface, Optimizations }
import org.nlogo.workspace.AbstractWorkspace
import org.nlogo.headless.test.{ LanguageTest, NormalMode, RunMode, TestMode }
import org.nlogo.core.Femto
import org.nlogo.util.{ TaggedFunSuite, ThreeDTag, TwoDTag }

object AbstractTestLanguage {
  implicit class RichMode(t: TestMode) {
    def tag: Tag =
      t match {
        case NormalMode => NormalModeTag
        case RunMode => RunModeTag
      }
  }

  object NormalModeTag extends Tag("org.nlogo.util.NormalModeTag")
  object RunModeTag extends Tag("org.nlogo.util.RunModeTag")

  case class TestSpace(modes: Seq[TestMode], versions: Seq[Version]) {
    def instances =
      for {
        m <- modes
        v <- versions
      } yield TestInstance(m, v)
  }

  case class TestInstance(mode: TestMode, version: Version) {
    val is3D = version.is3D
    def description = {
      val arityString = if (version.is3D) "3D" else "2D"
      val modeString =
        if (mode == NormalMode) "normal-mode"
        else "run-mode"
      s"($arityString, $modeString)"
    }
    def dialect = if (version.is3D) NetLogoThreeDDialect else NetLogoLegacyDialect
    def tags: Seq[Tag] = {
      val versionTag =
        if (version.is3D) ThreeDTag
        else TwoDTag
      Seq(versionTag, mode.tag)
    }
  }

  def spaceFromTest(t: LanguageTest): TestSpace = {
    import test._
    val versions =
      t.versionLimit match {
        case AnyVersion => Seq(TwoDVersion, ThreeDVersion)
        case TwoDOnly => Seq(TwoDVersion)
        case ThreeDOnly => Seq(ThreeDVersion)
        case HeadlessOnly => Seq()
      }
    TestSpace(t.modes, versions)
  }

  trait Runner {
    def workspace: HeadlessWorkspace
    def compiler: PresentationCompilerInterface
    def compilerFlags: CompilerFlags
    def cleanup(): Unit
    def defineProcedures(source: String): Unit
    def instance: AbstractTestLanguage.TestInstance
    def init(): Unit
    def initForTesting(size: Int): Unit
    def initForTesting(size: Int, modelString: String): Unit
    def newProgram: Program
    def openModel(model: Model): Unit
    def testCommand(command: String, agentClass: AgentKind = AgentKind.Observer): Unit
    def testCommandCompilerErrorMessage(command: String, errorMessage: String, agentClass: AgentKind = AgentKind.Observer): Unit
    def testCommandError(command: String, error: String, agentClass: AgentKind = AgentKind.Observer): Unit
    def testCommandErrorStackTrace(command: String, stackTrace: String, agentClass: AgentKind = AgentKind.Observer): Unit
    def testCompilerError(model: Model, errorMessage: String): Unit
    def testReporter(reporter: String, expectedResult: String): Unit
    def testReporterErrorStackTrace(reporter: String, stackTrace: String): Unit
    def testReporterError(reporter: String, error: String): Unit
  }
}

abstract class AbstractTestLanguage(tags: Tag*) extends TaggedFunSuite(tags: _*) {
  import AbstractTestLanguage.{ TestInstance, TestSpace }

  type Runner = AbstractTestLanguage.Runner

  val allConfigurations =
    TestSpace(Seq(NormalMode, RunMode), Seq(ThreeDVersion, TwoDVersion))

  val normalConfigurations =
    TestSpace(Seq(NormalMode), Seq(ThreeDVersion, TwoDVersion))

  val normalTwoD =
    TestSpace(Seq(NormalMode), Seq(TwoDVersion))

  val normalThreeD =
    TestSpace(Seq(NormalMode), Seq(ThreeDVersion))

  class RunnerImpl(val instance: TestInstance, makeOwner: AbstractWorkspace => JobOwner)
    extends Runner {

    val compilerFlags =
      if (instance.version.is3D) CompilerFlags(optimizations = Optimizations.gui3DOptimizations)
      else                       CompilerFlags(optimizations = Optimizations.guiOptimizations)

    val compiler =
      Femto.get[PresentationCompilerInterface]("org.nlogo.compile.Compiler", instance.dialect)

    def newProgram: Program = Program.fromDialect(instance.dialect)

    private var wsInitialized = false

    lazy val workspace = {
      val ws = HeadlessWorkspace.newInstance(instance.version.is3D)
      ws.silent = true
      wsInitialized = true
      ws
    }

    lazy val owner = makeOwner(workspace)

    def initForTesting(size: Int): Unit =
      initForTesting(size, "")

    def initForTesting(size: Int, modelString: String): Unit =
      workspace.initForTesting(instance.version.is3D, size, modelString)

    def openModel(model: Model): Unit =
      workspace.openModel(model.copy(version = instance.version.version))

    def defineProcedures(source: String) {
      val results = {
        compiler.compileProgram(
          HeadlessWorkspace.TestDeclarations + source, newProgram,
          workspace.getExtensionManager, workspace.getCompilationEnvironment, compilerFlags)
      }
      workspace.setProcedures(results.proceduresMap)
      workspace.world.asInstanceOf[CompilationManagement].program(results.program)
      workspace.init()
      workspace.world.realloc()
    }

    def testCommand(
      command: String,
      agentClass: AgentKind = AgentKind.Observer) {
        workspace.lastLogoException = null
        val runnableCommand =
          if (instance.mode == NormalMode) command
          else ("run \"" + org.nlogo.api.StringUtils.escapeString(command) + "\"")
        workspace.evaluateCommands(owner, runnableCommand,
          workspace.world.agentSetOfKind(agentClass), true)
        if(workspace.lastLogoException != null)
          throw workspace.lastLogoException
    }

    def testCommandError(command: String, error: String,
      agentClass: AgentKind = AgentKind.Observer) {
        try {
          testCommand(command, agentClass)
          fail("failed to cause runtime error: \"" + command + "\"")
        }
        catch {
          case ex: LogoException =>
            withClue(instance.mode + ": command: " + command) {
              assertResult(error)(ex.getMessage)
            }
        }
    }

    def testCommandErrorStackTrace(command: String, stackTrace: String, agentClass: AgentKind = AgentKind.Observer) {
        try {
          testCommand(command, agentClass)
          fail("failed to cause runtime error: \"" + command + "\"")
        }
        catch {
          case ex: LogoException =>
            withClue(instance.mode + ": command: " + command) {
              // println(workspace.lastErrorReport.stackTrace.get)
              assertResult(stackTrace)(workspace.lastErrorReport.stackTrace.get)
            }
        }
    }

    def testCommandCompilerErrorMessage(command: String, errorMessage: String, agentClass: AgentKind = AgentKind.Observer): Unit = {
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

    def testReporter(reporter: String, expectedResult: String) {
      workspace.lastLogoException = null
      val runnableReporter =
        if (instance.mode == NormalMode) reporter
        else ("runresult \"" + org.nlogo.api.StringUtils.escapeString(reporter) + "\"")
      val actualResult = workspace.evaluateReporter(owner, runnableReporter,
        workspace.world.observer)
      if(workspace.lastLogoException != null)
        throw workspace.lastLogoException
      // To be as safe as we can, let's do two separate checks here...
      // we'll compare the results both
      // as Logo values, and as printed representations.  Most of the time these checks will come out
      // the same, but it's good to have a both, partially as a way of giving both
      // Utils.recursivelyEqual() and Dump.logoObject() lots of testing! - ST 5/8/03
      withClue(instance.mode + ": not equals(): reporter \"" + reporter + "\"") {
        assertResult(expectedResult)(
          org.nlogo.api.Dump.logoObject(actualResult, true, false))
      }
      assert(Equality.equals(actualResult, compiler.readFromString(expectedResult)),
        instance.mode + ": not recursivelyEqual(): reporter \"" + reporter + "\"")
    }

    private def privateTestReporterError(reporter: String,
      expectedError: String,
      actualError: => Option[String]) {
        try {
          testReporter(reporter, expectedError)
          fail("failed to cause runtime error: \"" + reporter + "\"")
        } catch {
          // PureConstantOptimizer throws would-be runtime errors at compile-time, so we check those
          case ex: CompilerException
          if (ex.getMessage.startsWith(CompilerException.RuntimeErrorAtCompileTimePrefix)) =>
            assertResult(CompilerException.RuntimeErrorAtCompileTimePrefix + expectedError)(ex.getMessage)
          case ex: CompilerException =>
            fail("expected runtime error, got compile error: " + ex.getMessage)
          case ex: Throwable =>
            withClue(instance.mode + ": reporter: " + reporter) {
              if (actualError.isEmpty)
                fail("expected to find an error, but none found")
              else
                assertResult(expectedError)(actualError.get)
            }
        }
    }

    def testReporterError(reporter: String, error: String): Unit = {
      privateTestReporterError(reporter, error, Option(workspace.lastLogoException).map(_.getMessage))
    }

    def testReporterErrorStackTrace(reporter: String, stackTrace: String): Unit = {
      privateTestReporterError(reporter, stackTrace, Option(workspace.lastErrorReport).flatMap(_.stackTrace))
    }

    override def init(): Unit = {
      val dimensions =
        if (instance.version.is3D) new WorldDimensions3D(-5, 5, -5, 5, -5, 5)
        else                       new WorldDimensions(-5, 5, -5, 5)

      workspace.initForTesting(dimensions, "")
    }

    override def cleanup(): Unit = {
      if (wsInitialized)
        workspace.dispose()
    }
  }

  // override to customize workspace initialization
  def initializeRunner(r: Runner): Runner = {
    r.init()
    r
  }

  def cleanupRunner(r: Runner): Runner = {
    r.cleanup()
    r
  }

  def getOwner(workspace: AbstractWorkspace): JobOwner = workspace.defaultOwner

  def testInSpace(testName: String, configuration: TestSpace, testTags: Tag*)(testFun: Runner => Any)(implicit pos: Position) {
    testInSpaceWithOwner(testName, configuration, getOwner _, testTags: _*)(testFun)
  }

  def testInSpaceWithOwner(
    testName:      String,
    configuration: TestSpace,
    makeOwner:     AbstractWorkspace => JobOwner,
    testTags:      Tag*)(testFun: Runner => Any)(implicit pos: Position) {
    configuration.instances.foreach {
      i =>
        test(s"${testName} ${i.description}", (i.tags ++ testTags): _*) {
          val r = new RunnerImpl(i, getOwner _)
          initializeRunner(r)
          try {
            testFun(r)
          } finally {
            cleanupRunner(r)
          }
        }
    }
  }
}
