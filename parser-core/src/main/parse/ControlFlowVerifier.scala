// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import
  org.nlogo.core.{ AstTransformer, CommandBlock, Fail, I18N,
                   prim, ProcedureDefinition, ReporterApp, Statement },
    Fail._,
    prim.{ _commandlambda, _report, _run, _stop }

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
  case class CommandLambdaContext(nonLocalExit: Boolean)   extends CurrentContext {
    def exitsNonLocally = copy(true)
  }
  case class BlockContext(nonLocalExit: Boolean)           extends CurrentContext {
    def exitsNonLocally = copy(true)
  }

  var contextStack = List[CurrentContext]()

  override def visitProcedureDefinition(proc: ProcedureDefinition): ProcedureDefinition = {
    contextStack = if (proc.procedure.isReporter)
      new ReporterContext() :: contextStack
    else
      new CommandContext(false) :: contextStack
    val p = super.visitProcedureDefinition(proc)
    val ctx = contextStack.head
    contextStack = contextStack.tail
    p.copy(statements = p.statements.copy(nonLocalExit = ctx.nonLocalExit))
  }

  override def visitStatement(statement: Statement): Statement = {
    if (statement.command.isInstanceOf[_report] ||
      statement.command.isInstanceOf[_stop]     ||
      statement.command.isInstanceOf[_run])
      contextStack = contextStack.head.exitsNonLocally :: contextStack.tail
    (contextStack.head, statement.command) match {
      case (_: ReporterContext, _: _stop) =>
        exception(
          I18N.errors.getN("org.nlogo.prim.etc._stop.notAllowedInsideToReport", "STOP"),
          statement)
      case (_,                  _: _report) if contextStack.last.isInstanceOf[CommandContext] && ! contextStack.head.isInstanceOf[CommandLambdaContext] =>
        exception(
          I18N.errors.getN("org.nlogo.prim._report.canOnlyUseInToReport", "REPORT"),
          statement)
      case ((_: BlockContext | _: CommandContext), _: _report) =>
        exception(
          I18N.errors.getN("org.nlogo.prim._report.mustImmediatelyBeUsedInToReport", "REPORT"),
          statement)
      case _ if (statement.command.syntax.introducesContext) =>
        contextStack = new BlockContext(false) :: contextStack
        val r = super.visitStatement(statement)
        val ctx = contextStack.head
        contextStack = contextStack.tail
        val newArgs = statement.args.map {
          case c: CommandBlock => c.copy(
            statements = c.statements.copy(nonLocalExit = ctx.nonLocalExit))
          case other           => other
        }
        r.copy(args = newArgs)
      case _ =>
        super.visitStatement(statement)
    }
  }

  override def visitReporterApp(app: ReporterApp): ReporterApp = {
    app.reporter match {
      case _: _commandlambda =>
        contextStack = new CommandLambdaContext(false) :: contextStack
        val r = super.visitReporterApp(app)
        contextStack = contextStack.tail
        r
      case _ => super.visitReporterApp(app)
    }
  }
}
