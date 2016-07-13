// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.nlogo.core.{ I18N, Let }
import CompilerExceptionThrowers.cAssert

import org.nlogo.nvm.Procedure
import org.nlogo.prim.{ _reporterlambda, _reportertask, _lambdavariable, _letvariable, _procedurevariable, _taskvariable }

private class TaskVisitor extends DefaultAstVisitor {
  private var task = Option.empty[_reportertask]
  private var lambda = Option.empty[_reporterlambda]
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
      case l: _reporterlambda =>
        val old = lambda
        lambda = Some(l)
        super.visitReporterApp(expr)
        lambda = old
      case lv: _taskvariable =>
        val formal = task match {
          case None                   =>
            cAssert(procedure.get.isTask, I18N.errors.get("compiler.TaskVisitor.notDefined"), expr)
            procedure.get.getTaskFormal(s"?${lv.varNumber}").get
          case Some(l: _reportertask) => l.getFormal(lv.varNumber)
        }
        expr.reporter = new _letvariable(formal, formal.name)
        expr.reporter.copyMetadataFrom(lv)
      case lv: _lambdavariable =>
        if (! lambda.isDefined) {
          // only needed for command lambdas
          procedure match {
            case Some(proc) =>
              cAssert(proc.isTask, I18N.errors.get("compiler.TaskVisitor.notDefined"), expr)
              val formal = proc.getTaskFormal(lv.varName).get
              expr.reporter = new _letvariable(formal, lv.varName)
              expr.reporter.copyMetadataFrom(lv)
            case None => // TODO: Error here?
          }
        }
      case _ =>
        super.visitReporterApp(expr)
    }
  }
}
