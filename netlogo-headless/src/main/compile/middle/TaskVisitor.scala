// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package middle

import org.nlogo.{ core, nvm, prim }

// This replaces _lambdavariable with _letvariable everywhere.  And we need
//   to know which Let object to connect each occurrence to.
// There are two cases, command lambdas and reporter lambdas:
// - In the command lambda case, LambdaLifter already made the lambda body into
//   its own procedure, so we never see _commandlambda, so we look up the
//   right Let in the enclosing procedure.
// - In the reporter lambda case, we walk the tree and always keep track of
//   the nearest enclosing _reporterlambda node, so we can find our Let there.

class TaskVisitor extends DefaultAstVisitor {
  private var lambda = Option.empty[prim._reporterlambda]
  private var procedure = Option.empty[nvm.Procedure]

  override def visitProcedureDefinition(procdef: ProcedureDefinition) {
    procedure = Some(procdef.procedure)
    super.visitProcedureDefinition(procdef)
  }

  override def visitReporterApp(expr: ReporterApp) {
    expr.reporter match {
      case l: prim._reporterlambda =>
        val old = lambda
        lambda = Some(l)
        super.visitReporterApp(expr)
        lambda = old
      case lv: prim._lambdavariable =>
        val formal = lambda match {
          case None                          => procedure.get.getLambdaFormal(lv.varName).get
          case Some(l: prim._reporterlambda) => l.getFormal(lv.varName).get
        }
        val newLet = new prim._letvariable(formal)
        newLet.copyMetadataFrom(expr.reporter)
        expr.reporter = newLet
      case _ =>
        super.visitReporterApp(expr)
    }
  }
}
