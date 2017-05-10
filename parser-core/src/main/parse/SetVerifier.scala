// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{ AstVisitor, Fail, I18N, Reporter, ReporterApp, Statement, TypeNames, Variable },
  org.nlogo.core.prim.{ _set, _let, _letvariable, _reporterlambda},
  Fail._

object SetVerifier extends AstVisitor {
  override def visitStatement(stmt: Statement): Unit = {
    stmt.command match {
      case s: _set =>
        stmt.args.head match {
          case ReporterApp(v: Reporter with Variable, _, _) =>
          case ReporterApp(r: Reporter, args, loc) if r.syntax.isInfix =>
            fail(s"${r.displayName} expected ${TypeNames.aName(r.syntax.left)} on the left", args.head.sourceLocation)
          case exp =>
            fail(I18N.errors.get("compiler.SetVisitor.notSettable"), exp.sourceLocation)
        }
      case _ => super.visitStatement(stmt)
    }
  }
}
