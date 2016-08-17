// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang
package misc

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

import org.nlogo.api, api.LogoException
import org.nlogo.core.{Model, Plot, Pen, View}
import org.scalatest.FunSuite

class TestStackTraces extends FixtureSuite {

  def trace(implicit fixture: Fixture) = fixture.workspace.lastErrorReport.stackTrace.get.trim

  /// run/runresult tests

  val code =
    "to-report foo report bar end " +
    "to-report bar report __boom end"

  test("error inside run") { implicit fixture =>
    import fixture._
    open(Model(code))
    intercept[LogoException] {testCommand("__ignore runresult \"foo\"")}
    val expected =
      """|boom!
         |error while observer running __BOOM
         |  called by procedure BAR
         |  called by procedure FOO
         |  called by runresult
         |  called by procedure __EVALUATOR""".stripMargin
    assertResult(expected)(trace)
  }

  // ticket #1170
  test("error inside runresult") { implicit fixture =>
    import fixture._
    open(Model(code))
    intercept[LogoException] {testCommand("run \"__ignore foo\"")}
    val expected =
      """|boom!
         |error while observer running __BOOM
         |  called by procedure BAR
         |  called by procedure FOO
         |  called by run
         |  called by procedure __EVALUATOR""".stripMargin
    assertResult(expected)(trace)
  }

}
