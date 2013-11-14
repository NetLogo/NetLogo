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
import org.scalatest.FunSuite
import ModelCreator._

class TestStackTraces extends FixtureSuite {

  // these tests just call the primitive directly from the observer
  // the call should fail, and then we analyze the stack trace.
  // they are basically just simple little tests
  callPrimDirectly_Test(prim = "RESET-TICKS", codeType = PlotSetup)
  callPrimDirectly_Test(prim = "RESET-TICKS", codeType = PlotUpdate)
  callPrimDirectly_Test(prim = "RESET-TICKS", codeType = PenSetup)
  callPrimDirectly_Test(prim = "RESET-TICKS", codeType = PenUpdate)
  callPrimDirectly_Test(prim = "SETUP-PLOTS", codeType = PlotSetup)
  callPrimDirectly_Test(prim = "SETUP-PLOTS", codeType = PenSetup)
  callPrimDirectly_Test(prim = "TICK", codeType = PlotUpdate)
  callPrimDirectly_Test(prim = "TICK", codeType = PenUpdate)
  callPrimDirectly_Test(prim = "UPDATE-PLOTS", codeType = PlotUpdate)
  callPrimDirectly_Test(prim = "UPDATE-PLOTS", codeType = PenUpdate)

  // in these tests, the primitive is called indirectly from another procedure
  // therefore, it should be in the middle of the stack trace and have
  // many calls on either side. they are a little more advanced
  // but...nothing special. maybe we should merge the two eventually, but they are ok for now.
  callToPrimIsNested_Test(prim = "RESET-TICKS", codeType = PlotSetup)
  callToPrimIsNested_Test(prim = "RESET-TICKS", codeType = PlotUpdate)
  callToPrimIsNested_Test(prim = "RESET-TICKS", codeType = PenSetup)
  callToPrimIsNested_Test(prim = "RESET-TICKS", codeType = PenUpdate)
  callToPrimIsNested_Test(prim = "SETUP-PLOTS", codeType = PlotSetup)
  callToPrimIsNested_Test(prim = "SETUP-PLOTS", codeType = PenSetup)
  callToPrimIsNested_Test(prim = "TICK", codeType = PlotUpdate)
  callToPrimIsNested_Test(prim = "TICK", codeType = PenUpdate)
  callToPrimIsNested_Test(prim = "UPDATE-PLOTS", codeType = PlotUpdate)
  callToPrimIsNested_Test(prim = "UPDATE-PLOTS", codeType = PenUpdate)

  trait CodeType {
    val procName: String
    def plot(code: String): Plot
  }
  object PlotSetup extends CodeType {
    val procName = "plot 'p' setup code"
    def plot(code: String) =
      Plot(name = "p", pens = Pens(Pen(name = "pp")), setupCode = code)
  }
  object PlotUpdate extends CodeType {
    val procName = "plot 'p' update code"
    def plot(code: String) =
      Plot(name = "p", pens = Pens(Pen(name = "pp")), updateCode = code)
  }
  object PenSetup extends CodeType {
    val procName = "plot 'p' pen 'pp' setup code"
    def plot(code: String) =
      Plot(name = "p", pens = Pens(Pen(name = "pp", setupCode = code)))
  }
  object PenUpdate extends CodeType {
    val procName = "plot 'p' pen 'pp' update code"
    def plot(code: String) =
      Plot(name = "p", pens = Pens(Pen(name = "pp", updateCode = code)))
  }

  def trace(implicit fixture: Fixture) =
    fixture.workspace.lastErrorReport.stackTrace.get.trim

  def callPrimDirectly_Test(prim: String, codeType: CodeType) {
    test("direct call to " + prim + " with failure in " + codeType) { implicit fixture =>
      import fixture._
      open(Model("globals [x]", widgets = List(codeType.plot("if x = 1 [plot __boom]"))))
      testCommand("reset-ticks")
      testCommand("set x 1")
      intercept[LogoException] { testCommand(prim) }
      val expected =
        s"""|boom!
            |error while observer running __BOOM
            |  called by ${codeType.procName}
            |  called by $prim
            |  called by procedure __EVALUATOR""".stripMargin
      assertResult(expected)(trace)
    }
  }

  def callToPrimIsNested_Test(prim: String, codeType: CodeType) {
    val code = s"""|globals [x]
                   |to go1 go2 end
                   |to go2 go3 end
                   |to go3 $prim end
                   |to-report zero report 0 end
                   |to do-it if x = 1 [explode] end
                   |to explode print 1 / zero end
                   |""".stripMargin
    test("nesting " + prim + " in " + codeType) { implicit fixture =>
      import fixture._
      open(Model(code, widgets = List(codeType.plot("do-it"))))
      testCommand("reset-ticks")
      testCommand("set x 1")
      intercept[LogoException] {testCommand("go1")}
      val expected =
        s"""|Division by zero.
            |error while observer running /
            |  called by procedure EXPLODE
            |  called by procedure DO-IT
            |  called by ${codeType.procName}
            |  called by $prim
            |  called by procedure GO3
            |  called by procedure GO2
            |  called by procedure GO1
            |  called by procedure __EVALUATOR""".stripMargin
      assertResult(expected)(trace)
    }
  }

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
