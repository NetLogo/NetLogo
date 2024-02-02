// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package back

import org.nlogo.nvm
import org.nlogo.compile.api.{ DefaultAstVisitor, CommandBlock, Expression, ReporterBlock, ReporterApp, Statement }

/**
 * Fills the args arrays, in all of the Instructions anywhere in
 * the Procedure, with Reporters.
 */
private[compile] class ArgumentStuffer extends DefaultAstVisitor {
  override def visitStatement(stmt: Statement) {
    stmt.command.args = gatherArgs(stmt.args)
    super.visitStatement(stmt)
  }
  override def visitReporterApp(app: ReporterApp) {
    app.reporter.args = gatherArgs(app.args)
    super.visitReporterApp(app)
  }
  private def gatherArgs(expressions: Seq[Expression]): Array[nvm.Reporter] =
    expressions.flatMap{
      case app: ReporterApp => Some(app.reporter)
      case block: ReporterBlock => if (block.delayed) Some(block.reporter) else Some(block.app.reporter)
      case block: CommandBlock if block.delayed => Some(block.reporter)
      case _ => None
    }.toArray
}
