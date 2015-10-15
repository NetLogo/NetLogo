// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import
  org.nlogo.core.{ AstTransformer, CommandBlock, Fail, I18N,
                   prim, ProcedureDefinition, ReporterBlock, Statement, Statements },
    Fail._,
    prim.etc.{ _report, _run, _stop }

import
  scala.collection.mutable.Stack

class ControlFlowVerifier extends AstTransformer {

  sealed trait CurrentContext {
    def nonLocalExit: Boolean
    def exitsNonLocally: CurrentContext
  }

  case class ReporterContext(nonLocalExit: Boolean = true) extends CurrentContext {
    def exitsNonLocally = copy(true)
  }
  case class CommandContext(nonLocalExit: Boolean)         extends CurrentContext {
    def exitsNonLocally = copy(true)
  }
  case class BlockContext(nonLocalExit: Boolean)           extends CurrentContext {
    def exitsNonLocally = copy(true)
  }

  val contextStack = Stack[CurrentContext]()

  override def visitProcedureDefinition(proc: ProcedureDefinition): ProcedureDefinition = {
    if (proc.procedure.isReporter)
      contextStack.push(new ReporterContext())
    else
      contextStack.push(CommandContext(false))
    val p = super.visitProcedureDefinition(proc)
    val ctx = contextStack.pop()
    p.copy(
      statements = p.statements.copy(nonLocalExit = ctx.nonLocalExit))
  }

  override def visitStatement(statement: Statement): Statement = {
    if (statement.command.isInstanceOf[_report] ||
      statement.command.isInstanceOf[_stop]     ||
      statement.command.isInstanceOf[_run])
      contextStack.update(0, contextStack.head.exitsNonLocally)
    (contextStack.head, statement.command) match {
      case (_: ReporterContext, _: _stop) =>
        exception(
          I18N.errors.getN("org.nlogo.prim.etc._stop.notAllowedInsideToReport", "STOP"),
          statement)
      case (_,                  _: _report) if contextStack.last.isInstanceOf[CommandContext] =>
        exception(
          I18N.errors.getN("org.nlogo.prim._report.canOnlyUseInToReport", "REPORT"),
          statement)
      case ((_: BlockContext | _: CommandContext), _: _report) =>
        exception(
          I18N.errors.getN("org.nlogo.prim._report.mustImmediatelyBeUsedInToReport", "REPORT"),
          statement)
      case _ if (statement.command.syntax.introducesContext) =>
        contextStack.push(BlockContext(false))
        val r = super.visitStatement(statement)
        val context = contextStack.pop()
        val newArgs = statement.args.map {
          case c: CommandBlock => c.copy(
            statements = c.statements.copy(
              nonLocalExit = context.nonLocalExit))
          case other           => other
        }
        r.copy(args = newArgs)
      case _ =>
        super.visitStatement(statement)
    }
  }
}
