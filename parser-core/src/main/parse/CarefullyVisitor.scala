// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{AstTransformer, ReporterApp, Statement, I18N}
import org.nlogo.core.Fail._
import org.nlogo.core.prim._

/**
 * This is an AstVisitor that connects "error-message" reporters to
 * their enclosing "carefully" commands.
 */

class CarefullyVisitor extends AstTransformer {
  private var stack = List.empty[_carefully]
  override def visitStatement(stmt: Statement): Statement = {
    stmt.command match {
      case c:_carefully =>
        // carefully takes two arguments, both command blocks.
        // error-message is allowed only within the second block.
        val arg1 = visitExpression(stmt.args(0))
        stack = c::stack
        val arg2 = visitExpression(stmt.args(1))
        stack = stack.tail
        stmt.copy(args = Seq(arg1, arg2))
      case _ => super.visitStatement(stmt)
    }
  }
  override def visitReporterApp(app: ReporterApp): ReporterApp = {
    app.reporter match {
      case em: _errormessage =>
        if(stack.isEmpty)
          exception(I18N.errors.getN("compiler.CarefullyVisitor.badNesting", em.token.text), app)
        app.copy(reporter = em.copy(let = Option(stack.head.let)))
      case _ => super.visitReporterApp(app)
    }
  }
}
