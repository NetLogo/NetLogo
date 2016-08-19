// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.nlogo.core.{ I18N, Let }
import CompilerExceptionThrowers.cAssert

import org.nlogo.nvm.Procedure
import org.nlogo.prim.{ _commandlambda, _reporterlambda, _reportertask, _lambdavariable, _letvariable, _procedurevariable, _taskvariable }

import scala.collection.immutable.Stack

private object TaskVisitor {
  sealed trait FormalProvider {
    def letForName(varName: String): Option[Let]
  }

  case class CommandLambda(lambda: _commandlambda) extends FormalProvider {
    def letForName(varName: String): Option[Let] = lambda.proc.getTaskFormal(varName)
  }

  case class ReporterLambda(lambda: _reporterlambda) extends FormalProvider {
    def letForName(varName: String): Option[Let] = lambda.formals.find(_.name == varName)
  }

  case class LiftedLambda(procdef: ProcedureDefinition) extends FormalProvider {
    def letForName(varName: String): Option[Let] = procdef.procedure.getTaskFormal(varName)
  }
}

import TaskVisitor._

private class TaskVisitor extends DefaultAstVisitor {
  private var task = Option.empty[_reportertask]
  private var lambdaStack = Stack[FormalProvider]()
  private var procedure = Option.empty[Procedure]
  override def visitProcedureDefinition(procdef: ProcedureDefinition) {
    procedure = Some(procdef.procedure)

    if (procdef.procedure.isTask)
      lambdaStack = lambdaStack.push(LiftedLambda(procdef))

    super.visitProcedureDefinition(procdef)

    if (procdef.procedure.isTask)
      lambdaStack = lambdaStack.pop
  }

  override def visitReporterApp(expr: ReporterApp) {
    expr.reporter match {
      case l: _reportertask =>
        val old = task
        task = Some(l)
        super.visitReporterApp(expr)
        task = old
      case l: _reporterlambda =>
        lambdaStack = lambdaStack.push(ReporterLambda(l))
        super.visitReporterApp(expr)
        lambdaStack = lambdaStack.pop
      case c: _commandlambda =>
        lambdaStack = lambdaStack.push(CommandLambda(c))
        super.visitReporterApp(expr)
        lambdaStack = lambdaStack.pop
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
        val letsForVariable = lambdaStack.flatMap(_.letForName(lv.varName))
        letsForVariable.headOption match {
          case Some(let) =>
            expr.reporter = new _letvariable(let, lv.varName)
            expr.reporter.copyMetadataFrom(lv)
          case None =>
            cAssert(procedure.get.isTask, I18N.errors.getN("compiler.LocalsVisitor.notDefined", lv.varName), expr)
        }
      case _ =>
        super.visitReporterApp(expr)
    }
  }
}
