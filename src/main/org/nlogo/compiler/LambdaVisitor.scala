package org.nlogo.compiler

import CompilerExceptionThrowers.cAssert
import org.nlogo.api.{ I18N, Let }
import org.nlogo.nvm.{ Procedure, Reporter }
import org.nlogo.prim.{_lambda, _lambdareport, _letvariable, _lambdavariable}

private class LambdaVisitor extends DefaultAstVisitor {
  private var lambda = Option.empty[_lambdareport]
  private var procedure = Option.empty[Procedure]
  override def visitProcedureDefinition(procdef: ProcedureDefinition) {
    procedure = Some(procdef.procedure)
    super.visitProcedureDefinition(procdef)
  }
  override def visitReporterApp(expr: ReporterApp) {
    expr.reporter match {
      case l: _lambdareport =>
        val old = lambda
        lambda = Some(l)
        super.visitReporterApp(expr)
        lambda = old
      case lv: _lambdavariable =>
        lambda match {
          case None =>
            cAssert(procedure.get.isLambda,
                    I18N.errors.get("compiler.LambdaVisitor.notDefined"), expr)
            val formal: Let = procedure.get.getLambdaFormal(lv.varNumber, lv.token)
            expr.reporter = new _letvariable(formal, formal.varName)
            expr.reporter.token(lv.token)
          case Some(l: _lambdareport) =>
            val formal: Let = l.getFormal(lv.varNumber)
            expr.reporter = new _letvariable(formal, formal.varName)
            expr.reporter.token(lv.token)
        }
      case _ =>
        super.visitReporterApp(expr)
    }
  }
}
