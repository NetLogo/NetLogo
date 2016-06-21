// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

/**
Important Note:
LanguageTests (CommandTests and ReporterTests) now support testing stack traces directly.
Don't add any more tests here; add them there instead.

Here is an example from CommandTasks:

*command-task-stack-trace
   O> run task [print __boom] => STACKTRACE boom!\
   error while observer running __BOOM\
     called by (command task from: procedure __EVALUATOR)\
     called by procedure __EVALUATOR

notice the * at the start of the test name. this is to prevent it from
running in 'run' mode. in run mode, the stack trace is slightly different
since the top level procedure is run instead of __EVALUATOR.
its ok that these tests don't run in 'run' mode because we are only testing
the stack traces, not the results.
 */

import org.scalatest.FunSuite
import org.nlogo.api.{LogoException, ExtensionException, Argument, Context, Command, WorldDimensions3D, Version}
import org.nlogo.core.Syntax
import org.nlogo.workspace.{DummyClassManager, InMemoryExtensionLoader, ExtensionManager}
import org.nlogo.core.{WorldDimensions, View, Model}

class TestStackTraces extends AbstractTestModels {

  def trace = workspace.lastErrorReport.stackTrace.get.trim

  /// run/runresult tests

  val code =
    "to-report foo report bar end " +
    "to-report bar report __boom end"

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

}

class TestExtensionStackTraces extends FunSuite {
  test("extension exceptions keep causes") {
    val primaryCause = new Exception()
    val wrapperCause = new Exception(primaryCause)
    val dummyClassManager = new DummyClassManager() {
      override val barPrim = new Command {
        def getAgentClassString = "OTPL"
        override def getSyntax = Syntax.commandSyntax()
        override def perform(args: Array[Argument], context: Context) {
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
