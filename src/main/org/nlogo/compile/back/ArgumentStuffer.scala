// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile.back

import org.nlogo.nvm
import org.nlogo.compile.front

/**
 * Fills the args arrays, in all of the Instructions anywhere in
 * the Procedure, with Reporters.
 */
private class ArgumentStuffer extends front.DefaultAstVisitor {
  override def visitStatement(stmt: front.Statement) {
    stmt.command.args = gatherArgs(stmt.args)
    super.visitStatement(stmt)
  }
  override def visitReporterApp(app: front.ReporterApp) {
    app.reporter.args = gatherArgs(app.args)
    super.visitReporterApp(app)
  }
  private def gatherArgs(expressions: Seq[front.Expression]): Array[nvm.Reporter] =
    expressions.flatMap{
      case app: front.ReporterApp => Some(app.reporter)
      case block: front.ReporterBlock => Some(block.app.reporter)
      case _ => None
    }.toArray
}
