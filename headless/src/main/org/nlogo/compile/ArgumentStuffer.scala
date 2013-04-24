// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile

import org.nlogo.{ nvm, parse }

/**
 * Fills the args arrays, in all of the Instructions anywhere in
 * the Procedure, with Reporters.
 */
private class ArgumentStuffer extends parse.DefaultAstVisitor {
  override def visitStatement(stmt: parse.Statement) {
    stmt.command.args = gatherArgs(stmt.args)
    super.visitStatement(stmt)
  }
  override def visitReporterApp(app: parse.ReporterApp) {
    app.reporter.args = gatherArgs(app.args)
    super.visitReporterApp(app)
  }
  private def gatherArgs(expressions: Seq[parse.Expression]): Array[nvm.Reporter] =
    expressions.flatMap{
      case app: parse.ReporterApp => Some(app.reporter)
      case block: parse.ReporterBlock => Some(block.app.reporter)
      case _ => None
    }.toArray
}
