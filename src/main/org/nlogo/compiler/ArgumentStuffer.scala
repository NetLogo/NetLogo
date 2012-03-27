// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler
/**
 * Fills the args arrays, in all of the Instructions anywhere in
 * the Procedure, with Reporters.
 */
import org.nlogo.nvm.Reporter
private class ArgumentStuffer extends DefaultAstVisitor {
  override def visitStatement(stmt:Statement) {
    stmt.command.args = gatherArgs(stmt.args)
    super.visitStatement(stmt)
  }
  override def visitReporterApp(app:ReporterApp) {
    app.reporter.args = gatherArgs(app.args)
    super.visitReporterApp(app)
  }
  private def gatherArgs(expressions:Seq[Expression]):Array[Reporter] =
    expressions.flatMap{
      case app:ReporterApp => Some(app.reporter)
      case block:ReporterBlock => Some(block.app.reporter)
      case _ => None
    }.toArray
}
