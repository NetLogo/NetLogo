// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package middle

import org.nlogo.{ nvm, prim }

/**
 * Removes the bodies of command tasks and makes them into separate "child" procedures.
 */

class LambdaLifter(taskNumbers: Iterator[Int]) extends DefaultAstVisitor {
  val children = collection.mutable.Buffer[ProcedureDefinition]()
  private var procedure = Option.empty[nvm.Procedure]
  override def visitProcedureDefinition(procdef: ProcedureDefinition) {
    procedure = Some(procdef.procedure)
    super.visitProcedureDefinition(procdef)
  }
  override def visitReporterApp(expr: ReporterApp) {
    expr.reporter match {
      case c: prim._commandtask =>
        for(p <- procedure) {
          c.proc = new nvm.Procedure(
            false, "__task-" + taskNumbers.next(), c.token, Seq(), parent = p)
          c.proc.pos = expr.start
          c.proc.end = expr.end
          p.children += c.proc
          children +=
            new ProcedureDefinition(
              c.proc, expr.args(0).asInstanceOf[CommandBlock].statements)
          super.visitReporterApp(expr)
          expr.removeArgument(0)
        }
      case _ =>
        super.visitReporterApp(expr)
    }
  }
}
