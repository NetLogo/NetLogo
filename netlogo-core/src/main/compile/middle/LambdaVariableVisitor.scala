// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package middle

import
  org.nlogo.{ core, nvm, prim },
    core.{ Fail, I18N, Let },
      Fail.cAssert

import org.nlogo.compile.api.{ DefaultAstVisitor, ProcedureDefinition, ReporterApp, Statement }

import scala.collection.mutable.Stack

// This replaces _lambdavariable with _letvariable everywhere.  And we need
//   to know which Let object to connect each occurrence to.
// There are two cases, command lambdas and reporter lambdas:
// - In the command lambda case, LambdaLifter already made the lambda body into
//   its own procedure, so we never see _commandlambda, so we look up the
//   right Let in the enclosing procedure.
// - In the reporter lambda case, we walk the tree and always keep track of
//   the nearest enclosing _reporterlambda node, so we can find our Let there.

private object LambdaVariableVisitor {
  sealed trait FormalProvider {
    def letForName(varName: String): Option[Let]
  }

  case class CommandLambda(lambda: prim._commandlambda) extends FormalProvider {
    def letForName(varName: String): Option[Let] = lambda.proc.getLambdaFormal(varName)
  }

  case class ReporterLambda(lambda: prim._reporterlambda) extends FormalProvider {
    def letForName(varName: String): Option[Let] = lambda.formals.find(_.name == varName)
  }

  case class LiftedLambda(procdef: ProcedureDefinition) extends FormalProvider {
    def letForName(varName: String): Option[Let] = procdef.procedure match {
      case ll: nvm.LiftedLambda =>
        ll.getLambdaFormal(varName) orElse ll.closedLets.find(_.name.equalsIgnoreCase(varName))
      case _ => None
    }
  }
}

import LambdaVariableVisitor._

class LambdaVariableVisitor extends DefaultAstVisitor {
  private val lambdaStack = Stack[FormalProvider]()
  private var procedure = Option.empty[nvm.Procedure]

  override def visitProcedureDefinition(procdef: ProcedureDefinition): Unit = {
    procedure = Some(procdef.procedure)
    if (procdef.procedure.isLambda)
      lambdaStack.push(LiftedLambda(procdef))

    super.visitProcedureDefinition(procdef)

    if (procdef.procedure.isLambda)
      lambdaStack.pop()
  }

  override def visitStatement(stmt: Statement): Unit = {
    stmt.command match {
      case s: prim._set =>
        stmt.args(0) match {
          case ReporterApp(_, lv: prim._lambdavariable, _, _) =>
            val letsForVariable = lambdaStack.flatMap(_.letForName(lv.varName))
            letsForVariable.headOption match {
              case Some(let) =>
                val newCommand = new prim._setletvariable(let)
                newCommand.copyMetadataFrom(s)
                stmt.command = newCommand
                stmt.removeArgument(0)
              case None =>
                cAssert(procedure.get.isLambda, I18N.errors.getN("compiler.LocalsVisitor.notDefined", lv.varName), stmt)
            }
            super.visitStatement(stmt)
          case _ => super.visitStatement(stmt)
        }
      case _ => super.visitStatement(stmt)
    }
  }

  override def visitReporterApp(expr: ReporterApp): Unit = {
    expr.reporter match {
      case l: prim._reporterlambda =>
        lambdaStack.push(ReporterLambda(l))
        super.visitReporterApp(expr)
        lambdaStack.pop()
      case c: prim._commandlambda =>
        lambdaStack.push(CommandLambda(c))
        super.visitReporterApp(expr)
        lambdaStack.pop()
      case lv: prim._lambdavariable =>
        val letsForVariable = lambdaStack.flatMap(_.letForName(lv.varName))
        letsForVariable.headOption match {
          case Some(let) =>
            val newLet = new prim._letvariable(let)
            newLet.copyMetadataFrom(expr.reporter)
            expr.reporter = newLet
          case None =>
            cAssert(procedure.get.isLambda, I18N.errors.getN("compiler.LocalsVisitor.notDefined", lv.varName), expr)
        }
      case _ =>
        super.visitReporterApp(expr)
    }
  }
}
