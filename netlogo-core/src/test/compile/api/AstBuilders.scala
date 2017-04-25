// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile.api

import org.nlogo.core.{
  Command => CoreCommand, Reporter => CoreReporter, Femto, Instantiator,
  SourceLocation }
import org.nlogo.nvm.{ Command, Reporter }

class ReporterBuilder {
  val loc = SourceLocation(0, 0, "")

  var args = Seq.empty[Expression]
  var rep: Reporter = null
  var coreRep: CoreReporter = null

  def withReporter(cr: CoreReporter, r: Reporter): ReporterBuilder = {
    coreRep = cr
    rep = r
    this
  }

  def withArg(f: ReporterBuilder => ReporterBuilder): ReporterBuilder = {
    args :+= f(new ReporterBuilder()).build
    this
  }

  def withArg(a: Expression): ReporterBuilder = {
    args :+= a
    this
  }

  def constInt(i: Int): ReporterBuilder =
    const(Double.box(i), "_constdouble")

  def constString(s: String): ReporterBuilder =
    const(s, "_conststring")

  def const(a: AnyRef, primConstClass: String): ReporterBuilder = {
    coreRep = Femto.get[CoreReporter](Class.forName(s"org.nlogo.core.prim._const"), a)
    rep = Femto.get[Reporter](Class.forName(s"org.nlogo.prim.$primConstClass"), a)
    this
  }

  def reporterAppEtc(coreName: String, primName: String): ReporterBuilder = {
    coreRep = Instantiator.newInstance[CoreReporter](Class.forName(s"org.nlogo.core.prim.$coreName"))
    rep = Instantiator.newInstance[Reporter](Class.forName(s"org.nlogo.prim.$primName"))
    this
  }

  def build: ReporterApp = new ReporterApp(coreRep, rep, args, loc)

  def buildBlock: ReporterBlock = new ReporterBlock(build, loc)
}

// How to fix dependency problem:
// - we need to create nvm

trait StatementsBuilderBase {
  type ThisBuilder <: StatementsBuilderBase

  def thisBuilder: ThisBuilder

  val loc = SourceLocation(0, 0, "")

  var stmts = Seq.empty[Statement]

  def statement(coreCmd: CoreCommand, cmd: Command, args: Seq[Expression]): ThisBuilder = {
    stmts :+= new Statement(coreCmd, cmd, args, loc)
    thisBuilder
  }

  def statement(coreCmd: CoreCommand, cmd: Command): ThisBuilder = {
    stmts :+= new Statement(coreCmd, cmd, Seq.empty[Expression], loc)
    thisBuilder
  }

  def statementEtc(name: String, args: Seq[Expression]): ThisBuilder =
    statementEtc(name, name, args)

  def statementEtc(coreName: String, primName: String, args: Seq[Expression]): ThisBuilder =
    statementByName(s"org.nlogo.core.prim.$coreName", s"org.nlogo.prim.$primName", args)

  private def statementByName(
    coreClassName: String,
    primClassName: String,
    args: Seq[Expression] = Seq()): ThisBuilder = {
    val core = Instantiator.newInstance[CoreCommand](Class.forName(coreClassName))
    val prim = Instantiator.newInstance[Command](Class.forName(primClassName))
    stmts :+= new Statement(core, prim, args, loc)
    thisBuilder
  }

  def statementEtc(coreName: String, primName: String, args: Seq[Expression]): ThisBuilder =
    statementByName(s"org.nlogo.core.prim.$coreName", s"org.nlogo.prim.$primName", args)

  def build: Statements = new Statements(stmts, loc)

  def buildBlock: CommandBlock = new CommandBlock(build, loc)

  def stop =
    statementEtc("_stop", "etc._stop", Seq())

  def report(value: Expression): ThisBuilder =
    statementEtc("_report", "etc._report", Seq(value))

  def done: ThisBuilder =
    statementByName("org.nlogo.core.prim._done", "org.nlogo.prim._done")

  def returnreport: ThisBuilder =
    statementByName("org.nlogo.core.prim._done", "org.nlogo.prim._returnreport")
}

class StatementsBuilder extends StatementsBuilderBase {
  type ThisBuilder = StatementsBuilder
  def thisBuilder = this
}
