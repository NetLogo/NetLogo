// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core
package prim

// These classes make it easier to build ASTs for testing various AST manipulation.
// For instance, to build an AST for crt 5 [ fd 1 ]
//
// (new StatementsBuilder() {
//   statement(
//     _createturtles(null),
//     ReporterBuilder.constant(Double.box(5)),
//     (new StatementsBuilder() {
//       statement(_fd(), ReporterBuilder.constant(Double.box(1)))
//     }).buildBlock)
// }).build
//
// Note that the statements builder can be used to build a command block
// and the reporter builder can build a reporter block with `buildBlock`.
// Because these builders are composable / immutable, you can delay evaluation of the builder
// until the last minute and build multiple ASTs starting from the same base builder.

object ReporterBuilder {
  val loc = SourceLocation(0, 0, "")

  def constant(a: AnyRef): ReporterApp =
    new ReporterApp(_const(a), Seq.empty[Expression], loc)
}

class ReporterBuilder {
  import ReporterBuilder.loc

  var args = Seq.empty[Expression]
  var rep: Reporter = null

  def withReporter(r: Reporter): ReporterBuilder = {
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

  def build: ReporterApp = new ReporterApp(rep, args, loc)

  def buildBlock: ReporterBlock = new ReporterBlock(build, loc)
}

trait StatementsBuilderBase {
  type ThisBuilder <: StatementsBuilderBase

  def thisBuilder: ThisBuilder

  val loc = SourceLocation(0, 0, "")

  var stmts = Seq.empty[Statement]

  def statement(cmd: Command, args: Seq[Expression]): ThisBuilder = {
    stmts :+= new Statement(cmd, args, loc)
    thisBuilder
  }

  def statement(cmd: Command): ThisBuilder = {
    stmts :+= new Statement(cmd, Seq.empty[Expression], loc)
    thisBuilder
  }

  def build: Statements = new Statements("", stmts)

  def buildBlock: CommandBlock = new CommandBlock(build, loc)
}

class StatementsBuilder extends StatementsBuilderBase {
  type ThisBuilder = StatementsBuilder
  def thisBuilder = this
}
