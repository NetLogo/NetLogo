// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import CompilerExceptionThrowers.cAssert
import org.nlogo.api.{ I18N, Let }
import org.nlogo.nvm.Procedure
import org.nlogo.prim.{ _reportertask, _letvariable, _taskvariable }

private class TaskVisitor extends DefaultAstVisitor {
  private var task = Option.empty[_reportertask]
  private var procedure = Option.empty[Procedure]
  override def visitProcedureDefinition(procdef: ProcedureDefinition) {
    procedure = Some(procdef.procedure)
    super.visitProcedureDefinition(procdef)
  }
  override def visitReporterApp(expr: ReporterApp) {
    expr.reporter match {
      case l: _reportertask =>
        val old = task
        task = Some(l)
        super.visitReporterApp(expr)
        task = old
      case lv: _taskvariable =>
        task match {
          case None =>
            cAssert(procedure.get.isTask,
                    I18N.errors.get("compiler.TaskVisitor.notDefined"), expr)
            val formal: Let = procedure.get.getTaskFormal(lv.varNumber, lv.token)
            expr.reporter = new _letvariable(formal, formal.varName)
            expr.reporter.token(lv.token)
          case Some(l: _reportertask) =>
            val formal: Let = l.getFormal(lv.varNumber)
            expr.reporter = new _letvariable(formal, formal.varName)
            expr.reporter.token(lv.token)
        }
      case _ =>
        super.visitReporterApp(expr)
    }
  }
}
