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

import org.nlogo.api.LogoException

class TestStackTraces extends AbstractTestModels {

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

  def trace = workspace.lastErrorReport.stackTrace.get.trim

  def callPrimDirectly_Test(prim: String, codeType: CodeType) {
    testModel("direct call to " + prim + " with failure in " + codeType,
      Model("globals [x]", widgets = List(codeType.plot("if x = 1 [plot __boom]")))) {
      observer >> "reset-ticks"
      observer >> "set x 1"
      intercept[LogoException] {observer >> prim}
      assert(trace === """boom!
error while observer running __BOOM
  called by """ + codeType.procName + """
  called by """ + prim + """
  called by procedure __EVALUATOR""")
    }
  }

  def callToPrimIsNested_Test(prim: String, codeType: CodeType) {
    val code = """
  globals [x]
  to go1 go2 end
  to go2 go3 end
  to go3 """ + prim + """ end
  to-report zero report 0 end
  to do-it if x = 1 [explode] end
  to explode print 1 / zero end
"""
    testModel("nesting " + prim + " in " + codeType, Model(code, widgets = List(codeType.plot("do-it")))) {
      observer >> "reset-ticks"
      observer >> "set x 1"
      intercept[LogoException] {observer >> "go1"}
      assert(trace === """Division by zero.
error while observer running /
  called by procedure EXPLODE
  called by procedure DO-IT
  called by """ + codeType.procName + """
  called by """ + prim + """
  called by procedure GO3
  called by procedure GO2
  called by procedure GO1
  called by procedure __EVALUATOR""")
    }
  }

  /// run/runresult tests

  val code =
    "to-report foo report bar end " +
    "to-report bar report __boom end"

  testModel("error inside run", Model(code)) {
    intercept[LogoException] {observer >> "__ignore runresult \"foo\""}
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

/*
  // return address bug
  val code2 =
  """to go my-update-plots tick end
     to my-update-plots print __boom end"""

  testModel("hmmm", Model(code2)) {
    intercept[LogoException] {observer >> "go"}
    assert(trace === """boom!
error while observer running __BOOM
  called by procedure MY-UPDATE-PLOTS
  called by procedure GO
  called by procedure __EVALUATOR""")
  }
*/

}
