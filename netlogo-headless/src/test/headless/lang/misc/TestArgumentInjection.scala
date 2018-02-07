// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang
package misc

// NetLogo's job manager lacks a way to submit a job along with inputs; you can only submit no-input
// procedures.  This lack is issue #413.  This file contains semi-official sample code for working
// around it.
//
// The plan is: compile a dummy no-input procedure that calls the procedure we really want to call,
// passing it some dummy arguments.  Then manually modify the Procedure object we get back so it
// contains the arguments we want, and submit the job.
//
// Note that the same Procedure object can be modified and re-submitted multiple times, without
// having to re-invoke the compiler.

// Among the awkward details here:
//
// - If the bytecode generator is on, the compiler will bytecode-generate the dummy arguments
//   out of existence, so we need to invoke the compiler with useGenerator = false.
//
// - We use AbstractWorkspace because the methods on nvm.Workspace for invoking the
//   compiler don't allow passing altered CompilerFlags.  (I'd like to change it so they did,
//   but I'm reluctant to make life too awkward for people using nvm.Workspace from Java.)

import org.nlogo.{ core, api, nvm, workspace => wspackage },
  core.AgentKind,
    AgentKind.Observer,
  wspackage.{ AbstractWorkspace },
  nvm.{ CompilerFlags, Optimizations }

class TestArgumentInjection extends FixtureSuite {

  override def makeWorkspace() = {
    HeadlessWorkspace.newInstance(classOf[HeadlessWorkspace],
      CompilerFlags(optimizations = Optimizations.headlessOptimizations, useGenerator = false))
  }

  // making callers

  def substituteArgs(instr: nvm.Instruction, args: AnyRef*) {
    for(i <- args.indices) {
      instr.args(i) = instr.workspace.compiler.makeLiteralReporter(args(i))
      instr.args(i).init(instr.workspace)
    }
  }

  def makeCommandCaller(ws: AbstractWorkspace, name: String): nvm.Procedure = {
    implicit val extensionManager = ws.extensionManager
    implicit val compilationEnvironment = ws.getCompilationEnvironment
    implicit val procedures = ws.procedures
    implicit val linker = ws.linker

    val proc = ws.procedures(name.toUpperCase)
    val command =
      name + " " + Seq.fill(proc.args.size)("0").mkString(" ")
    ws.evaluator.compileCommands(command, Observer)
  }

  def makeReporterCaller(ws: AbstractWorkspace, name: String): nvm.Procedure = {
    implicit val extensionManager = ws.extensionManager
    implicit val compilationEnvironment = ws.getCompilationEnvironment
    implicit val procedures = ws.procedures
    implicit val linker = ws.linker

    val proc = ws.procedures(name.toUpperCase)
    val dummyArgs = Seq.fill(proc.args.size)("0").mkString(" ")
    ws.evaluator.compileReporter(s"$name $dummyArgs")
  }

  // using callers

  def is(i: nvm.Instruction, name: String) =
    i.getClass.getSimpleName == name

  def runCommandCaller(owner: api.JobOwner, caller: nvm.Procedure, args: AnyRef*) {
    val call = caller.code.find(is(_, "_call")).get
    substituteArgs(call, args: _*)
    call.workspace.runCompiledCommands(owner, caller)
  }

  def runReporterCaller(owner: api.JobOwner, caller: nvm.Procedure, args: AnyRef*): AnyRef = {
    val call =
      caller.code.find(is(_, "_report")).get
        .args.find(is(_, "_callreport")).get
    substituteArgs(call, args: _*)
    call.workspace.runCompiledReporter(owner, caller)
  }

  // test multiplying two numbers

  test("multiply via command procedure") { implicit fixture =>
    import fixture._
    declare("""|globals [g]
               |to foo [a b]
               |  set g a * b
               |end""".stripMargin)
    val caller = makeCommandCaller(workspace, "foo")
    def multiply(a: Double, b: Double): Double = {
      runCommandCaller(owner(), caller, Double.box(a), Double.box(b))
      Double.unbox(workspace.report("g"))
    }
    assertResult(12)(multiply(3, 4))
    assertResult(30)(multiply(5, 6))
  }

  test("multiply via reporter procedure") { implicit fixture =>
    import fixture._
    declare("""|globals [g]
               |to-report bar [a b]
               |  report a * b
               |end""".stripMargin)
    val caller = makeReporterCaller(workspace, "bar")
    def multiply(a: Double, b: Double): Double =
      Double.unbox(
        runReporterCaller(owner(), caller, Double.box(a), Double.box(b)))
    assertResult(12)(multiply(3, 4))
    assertResult(30)(multiply(5, 6))
  }

  // it works with agents too, not just simple math

  test("turtles and links") { implicit fixture =>
    import fixture._
    declare("to make-network [n] ca cro n [ create-links-with other turtles ] end")
    val caller = makeCommandCaller(workspace, "make-network")
    def check(n: Int) {
      runCommandCaller(owner(), caller, Double.box(n))
      assertResult(Double.box(n * (n - 1) / 2))(
        workspace.report("count links"))
    }
    check(5)
    check(10)
  }
}
