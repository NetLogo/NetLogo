// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.nlogo.{ core, nvm, prim => coreprim },
  core.Let

/**
 * Removes the bodies of command lambdas and makes them into separate "child" procedures.
 */

class LambdaLifter(lambdaNumbers: Iterator[Int]) extends AstTransformer {
  val children = collection.mutable.Buffer[ProcedureDefinition]()
  private var procedures = List.empty[nvm.Procedure]

  override def visitProcedureDefinition(procdef: ProcedureDefinition): ProcedureDefinition = {
    procedures = procdef.procedure::procedures
    val newProcedure = super.visitProcedureDefinition(procdef)
    procedures = procedures.tail
    newProcedure
  }

  override def visitReporterApp(expr: ReporterApp): ReporterApp = {
    expr.reporter match {
      case c: coreprim._commandlambda =>
        procedures.lastOption.map { p =>
          val formals = c.argumentNames.map(n => Let(n)).toArray
          val name = "__lambda-" + lambdaNumbers.next()
          c.proc =
            new nvm.Procedure(false, c.token, name, None, parent = procedures.head, lambdaFormals = formals)
          c.proc.pos = expr.start
          c.proc.end = expr.end
          p.children += c.proc

          val commandBlock = expr.args(0).asInstanceOf[CommandBlock]
          children += new ProcedureDefinition(c.proc, commandBlock.statements)

          procedures = c.proc::procedures
          val newExpr = super.visitReporterApp(expr)
          procedures = procedures.tail
          newExpr.copy(args = newExpr.args.tail)
        }.get
      case _ =>
        super.visitReporterApp(expr)
    }
  }
}
