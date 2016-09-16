// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.nlogo.core.{ ClosedLet, ClosedLambdaVariable, I18N, Let }
import CompilerExceptionThrowers.cAssert

import org.nlogo.nvm.{ LiftedLambda => NvmLiftedLambda, Procedure }
import org.nlogo.prim.{ _commandlambda, _reporterlambda, _lambdavariable, _letvariable, _procedurevariable, _set, _setletvariable }

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
    def letForName(varName: String): Option[Let] = procdef.procedure match {
      case ll: NvmLiftedLambda =>
        ll.getLambdaFormal(varName) orElse ll.closedLets.find(_.name.equalsIgnoreCase(varName))
      case _ => None
    }
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

  override def visitStatement(stmt: Statement): Unit = {
    stmt.command match {
      case s: _set =>
        stmt.args(0) match {
          case ReporterApp(_, lv: _lambdavariable, _, _) =>
            val letsForVariable = lambdaStack.flatMap(_.letForName(lv.varName))
            val newStmt = letsForVariable.headOption match {
              case Some(let) =>
                val newCommand = new _setletvariable(let, lv.varName)
                newCommand.copyMetadataFrom(s)
                super.visitStatement(stmt.copy(command = newCommand, args = stmt.args.tail))
              case None =>
                cAssert(procedure.get.isLambda, I18N.errors.getN("compiler.LocalsVisitor.notDefined", lv.varName), stmt)
            }
            super.visitStatement(stmt)
          case _ => super.visitStatement(stmt)
        }
      case _ => super.visitStatement(stmt)
    }
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
