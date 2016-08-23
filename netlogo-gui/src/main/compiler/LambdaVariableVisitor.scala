// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.nlogo.core.{ I18N, Let }
import CompilerExceptionThrowers.cAssert

import org.nlogo.nvm.Procedure
import org.nlogo.prim.{ _commandlambda, _reporterlambda, _lambdavariable, _letvariable, _procedurevariable }

import scala.collection.immutable.Stack

private object LambdaVariableVisitor {
  sealed trait FormalProvider {
    def letForName(varName: String): Option[Let]
  }

  case class CommandLambda(lambda: _commandlambda) extends FormalProvider {
    def letForName(varName: String): Option[Let] = lambda.proc.getLambdaFormal(varName)
  }

  case class ReporterLambda(lambda: _reporterlambda) extends FormalProvider {
    def letForName(varName: String): Option[Let] = lambda.formals.find(_.name == varName)
  }

  case class LiftedLambda(procdef: ProcedureDefinition) extends FormalProvider {
    def letForName(varName: String): Option[Let] = procdef.procedure.getLambdaFormal(varName)
  }
}

import LambdaVariableVisitor._

private class LambdaVariableVisitor extends DefaultAstVisitor {
  private var lambdaStack = Stack[FormalProvider]()
  private var procedure = Option.empty[Procedure]
  override def visitProcedureDefinition(procdef: ProcedureDefinition) {
    procedure = Some(procdef.procedure)

    if (procdef.procedure.isLambda)
      lambdaStack = lambdaStack.push(LiftedLambda(procdef))

    super.visitProcedureDefinition(procdef)

    if (procdef.procedure.isLambda)
      lambdaStack = lambdaStack.pop
  }

  override def visitReporterApp(expr: ReporterApp) {
    expr.reporter match {
      case l: _reporterlambda =>
        lambdaStack = lambdaStack.push(ReporterLambda(l))
        super.visitReporterApp(expr)
        lambdaStack = lambdaStack.pop
      case c: _commandlambda =>
        lambdaStack = lambdaStack.push(CommandLambda(c))
        super.visitReporterApp(expr)
        lambdaStack = lambdaStack.pop
      case lv: _lambdavariable =>
        val letsForVariable = lambdaStack.flatMap(_.letForName(lv.varName))
        letsForVariable.headOption match {
          case Some(let) =>
            expr.reporter = new _letvariable(let, lv.varName)
            expr.reporter.copyMetadataFrom(lv)
          case None =>
            cAssert(procedure.get.isLambda, I18N.errors.getN("compiler.LocalsVisitor.notDefined", lv.varName), expr)
        }
      case _ =>
        super.visitReporterApp(expr)
    }
  }
}
