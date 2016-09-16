// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.nlogo.{ core, nvm, prim => enginePrim },
  core.{ ClosedLambdaVariable, ClosedLet, ClosedVariable, Let },
  enginePrim.{ _commandlambda, _reporterlambda }

/**
 * Removes the bodies of command lambdas and makes them into separate "child" procedures.
 */

class LambdaLifter(lambdaNumbers: Iterator[Int]) extends AstTransformer {
  val children = collection.mutable.Buffer[ProcedureDefinition]()
  private var procedures = List.empty[nvm.Procedure]
  private var lambdaStack = List[Either[_commandlambda,_reporterlambda]]()

  override def visitProcedureDefinition(procdef: ProcedureDefinition): ProcedureDefinition = {
    procedures = procdef.procedure::procedures
    val newProcedure = super.visitProcedureDefinition(procdef)
    procedures = procedures.tail
    newProcedure
  }

  override def visitReporterApp(expr: ReporterApp): ReporterApp = {
    expr.reporter match {
      case r: _reporterlambda=>
        lambdaStack = Right(r)::lambdaStack
        val res = super.visitReporterApp(expr)
        lambdaStack = lambdaStack.tail
        res
      case c: _commandlambda =>
        procedures.headOption.map { p =>
          val formals = c.argumentNames.map(n => Let(n)).toArray
          val name = "__lambda-" + lambdaNumbers.next()
          c.proc =
            new nvm.LiftedLambda(false, c.token, name, None, parent = procedures.head, lambdaFormals = formals, closedLets = resolveClosedVariables(c.closedVariables))
          c.proc.pos = expr.start
          c.proc.end = expr.end
          p.children += c.proc

          val commandBlock = expr.args(0).asInstanceOf[CommandBlock]
          children += new ProcedureDefinition(c.proc, commandBlock.statements)

          procedures = c.proc::procedures
          lambdaStack = Left(c)::lambdaStack
          val newExpr = super.visitReporterApp(expr)
          lambdaStack = lambdaStack.tail
          procedures = procedures.tail
          newExpr.copy(args = newExpr.args.tail)
        }.get
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
