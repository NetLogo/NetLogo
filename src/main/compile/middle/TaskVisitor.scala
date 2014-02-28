// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package middle

import org.nlogo.{ api, nvm, prim },
  Fail._

private class TaskVisitor extends DefaultAstVisitor {
  private var task = Option.empty[prim._reportertask]
  private var procedure = Option.empty[nvm.Procedure]
  override def visitProcedureDefinition(procdef: ProcedureDefinition) {
    procedure = Some(procdef.procedure)
    super.visitProcedureDefinition(procdef)
  }
  override def visitReporterApp(expr: ReporterApp) {
    expr.reporter match {
      case l: prim._reportertask =>
        val old = task
        task = Some(l)
        super.visitReporterApp(expr)
        task = old
      case lv: prim._taskvariable =>
        task match {
          case None =>
            cAssert(procedure.get.isTask,
                    api.I18N.errors.get("compiler.TaskVisitor.notDefined"), expr)
            val formal: api.Let = procedure.get.getTaskFormal(lv.varNumber, lv.token)
            expr.reporter = new prim._letvariable(formal)
            expr.reporter.token(lv.token)
          case Some(l: prim._reportertask) =>
            val formal: api.Let = l.getFormal(lv.varNumber)
            expr.reporter = new prim._letvariable(formal)
            expr.reporter.token(lv.token)
        }
      case _ =>
        super.visitReporterApp(expr)
    }
  }
}
