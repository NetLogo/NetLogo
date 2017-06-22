// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package middle

import org.nlogo.{ core, nvm, prim },
  core.{ ClosedLambdaVariable, ClosedLet, ClosedVariable, Let },
  prim.{ _commandlambda, _reporterlambda }

import org.nlogo.compile.api.{ AstTransformer, CommandBlock, ProcedureDefinition, ReporterApp }

/**
 * Removes the bodies of command lambdas and makes them into separate "child" procedures.
 */

class LambdaLifter(lambdaNumbers: Iterator[Int]) extends AstTransformer {
  val children = collection.mutable.Buffer[ProcedureDefinition]()
  private var procedures = List.empty[nvm.Procedure]
  private var lambdaStack = List.empty[Either[_commandlambda,_reporterlambda]]

  override def visitProcedureDefinition(procdef: ProcedureDefinition): ProcedureDefinition = {
    procedures = procdef.procedure::procedures
    val newProcedure = super.visitProcedureDefinition(procdef)
    procedures = procedures.tail
    newProcedure
  }

  override def visitReporterApp(expr: ReporterApp): ReporterApp = {
    expr.reporter match {
      case r: _reporterlambda =>
        lambdaStack = Right(r)::lambdaStack
        val res = super.visitReporterApp(expr)
        lambdaStack = lambdaStack.tail
        res
      case c: prim._commandlambda =>
        val p = procedures.head
        val formals = c.argumentNames.map(n => Let(n))
        val name = "__lambda-" + lambdaNumbers.next()
        c.proc = new nvm.LiftedLambda(
          name, c.token, c.argTokens, parent = p, lambdaFormals = formals,
          closedLets = resolveClosedVariables(c.closedVariables), c.source)
        c.proc.pos = expr.start
        c.proc.end = expr.end
        p.addChild(c.proc)

        children +=
          new ProcedureDefinition(c.proc, expr.args(0).asInstanceOf[CommandBlock].statements)

        procedures = c.proc::procedures
        lambdaStack = Left(c)::lambdaStack
        val newExpr = super.visitReporterApp(expr)
        lambdaStack = lambdaStack.tail
        procedures = procedures.tail
        newExpr.copy(args = newExpr.args.tail)
      case _ =>
        super.visitReporterApp(expr)
    }
  }

  private def resolveClosedVariables(closedVars: Set[ClosedVariable]): Set[Let] = {
    closedVars.flatMap {
      case ClosedLet(let) => Some(let)
      case ClosedLambdaVariable(name) =>
        lambdaStack.flatMap {
          case Left(cl)  => cl.proc.lambdaFormals.find(_.name.equalsIgnoreCase(name))
          case Right(rl) => rl.formals.find(_.name.equalsIgnoreCase(name))
        }
    }
  }
}
