// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

/**
Important Note:
LanguageTests (CommandTests and ReporterTests) now support testing stack traces directly.
Don't add any more tests here; add them there instead.

Here is an example from CommandTasks:


*command-task-stack-trace
  O> run [[] -> print __boom] => STACKTRACE boom!\
   error while observer running __BOOM\
     called by (anonymous command from: procedure __EVALUATOR)\
     called by procedure __EVALUATOR

notice the * at the start of the test name. this is to prevent it from
running in 'run' mode. in run mode, the stack trace is slightly different
since the top level procedure is run instead of __EVALUATOR.
its ok that these tests don't run in 'run' mode because we are only testing
the stack traces, not the results.
 */

import org.scalatest.funsuite.AnyFunSuite
import org.nlogo.api.{LogoException, ExtensionException, Argument, Context, Command, Version}
import org.nlogo.core.{ Syntax, WorldDimensions3D }
import org.nlogo.workspace.{DummyClassManager, InMemoryExtensionLoader}
import org.nlogo.core.{WorldDimensions, View, Model}

class TestStackTraces extends AbstractTestModels {

  def trace = workspace.lastErrorReport.stackTrace.get.trim

  /// run/runresult tests

  val code =
    """|to-report foo report bar end
       |to-report bar report __boom end
       |to-report overflow report runresult "runresult \"overflow\"" end
       |to overflow-run run [ [] -> run [ [] -> overflow-run ] ] end
       |to overflow-foreach foreach [1 2 3] [ [i] -> overflow-foreach ] end""".stripMargin

  testModel("error inside run", Model(code)) {
    intercept[LogoException] { observer >> "__ignore runresult \"foo\"" }
    assert(trace === """boom!
error while observer running __BOOM
  called by procedure BAR
  called by procedure FOO
  called by runresult
  called by procedure __EVALUATOR""")
  }

  // ticket #1170
  testModel("error inside runresult", Model(code)) {
    intercept[LogoException] {observer >> "run \"__ignore foo\""}
    assert(trace === """boom!
error while observer running __BOOM
  called by procedure BAR
  called by procedure FOO
  called by run
  called by procedure __EVALUATOR""")
  }

  testModel("stack overflow - command", Model(code)) {
    intercept[LogoException] { observer >> "overflow-run" }
    assert(trace.take(2000).startsWith(
      """|stack overflow (recursion too deep)
         |  error while observer running RUN""".stripMargin))
    trace.linesIterator.drop(2).take(100).toSeq.dropRight(1).foreach(l =>
        assert(l === "  called by run" ||
          l === "  called by procedure OVERFLOW-RUN"))
  }

  testModel("stack overflow - reporter", Model(code)) {
    intercept[LogoException] { observer >> "show overflow" }
    assert(trace.take(2000).startsWith(
      """|stack overflow (recursion too deep)
         |  error while observer running REPORT""".stripMargin))
    trace.linesIterator.drop(2).take(100).foreach(l =>
        assert(l === "  called by runresult" ||
          l === "  called by procedure OVERFLOW"))
  }


  testModel("stack overflow - foreach", Model(code)) {
    intercept[LogoException] { observer >> "overflow-foreach" }
    assert(trace.take(2000).startsWith(
      """|stack overflow (recursion too deep)
         |  error while observer running FOREACH""".stripMargin))
    trace.linesIterator.drop(2).take(100).toSeq.dropRight(1).foreach(l =>
        assert(l === "  called by foreach" ||
          l === "  called by procedure OVERFLOW-FOREACH"))
  }
}

class TestExtensionStackTraces extends AnyFunSuite {
  test("extension exceptions keep causes") {
    val primaryCause = new Exception()
    val wrapperCause = new Exception(primaryCause)
    val dummyClassManager = new DummyClassManager() {
      override val barPrim = new Command {
        override def getSyntax = Syntax.commandSyntax()
        override def perform(args: Array[Argument], context: Context): Unit = {
          throw new ExtensionException(wrapperCause)
        }
      }
    }

    val memoryLoader = new InMemoryExtensionLoader("foo", dummyClassManager)
    val ws = HeadlessWorkspace.newInstance
    ws.getExtensionManager.addLoader(memoryLoader)
    val dims = if(Version.is3D)
                 new WorldDimensions3D(-5, 5, -5, 5, -5, 5)
               else
                 new WorldDimensions(-5, 5, -5, 5)
    ws.initForTesting(dims, "")
    ws.openModel(Model(
      code = "extensions [ foo ]",
      widgets = List(View(dimensions = dims)),
      version = Version.version
    ))

    try {
      ws.command("foo:bar")
    } catch {
      case e: org.nlogo.nvm.EngineException => {
        var ex: Throwable = e
        while (ex != null && ex != wrapperCause) ex = ex.getCause
        assert(ex === wrapperCause)
        assert(ex.getCause === primaryCause)
      }
    }
  }
}
