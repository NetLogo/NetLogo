// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.nlogo.api.I18N
import org.nlogo.compiler.CompilerExceptionThrowers._
import org.nlogo.prim._

/**
 * This is an AstVisitor that connects "error-message" reporters to
 * their enclosing "carefully" commands.
 */

private class CarefullyVisitor extends DefaultAstVisitor {
  private val stack = new collection.mutable.Stack[_carefully]
  override def visitStatement(stmt:Statement) {
    stmt.command match {
      case c:_carefully =>
        // carefully takes two arguments, both command blocks.
        // error-message is allowed only within the second block.
        stmt(0).accept(this)
        stack.push(c)
        stmt(1).accept(this)
        stack.pop()
      case _ => super.visitStatement(stmt)
    }
  }
  override def visitReporterApp(app:ReporterApp) {
    app.reporter match {
      case em: _errormessage =>
        if(stack.isEmpty)
          exception(I18N.errors.getN("compiler.CarefullyVisitor.badNesting", em.token.name), app)
        em.let = stack.top.let
      case _ => super.visitReporterApp(app)
    }
  }
}
