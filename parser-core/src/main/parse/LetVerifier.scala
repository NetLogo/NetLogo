// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core._,
  org.nlogo.core.prim.{_let, _letvariable},
  Fail._

// This ensures that no statements of the form `let x x` are allowed in the AST.
// This would be *possible* to do in LetScoper, but difficult because we don't yet understand
// the shape of the AST.
class LetVerifier extends AstVisitor {
  var currentLet = Option.empty[Let]

  override def visitStatement(stmt: Statement): Unit = {
    stmt.command match {
      case l: _let =>
        currentLet = l.let
        super.visitStatement(stmt)
        currentLet = None
      case _ => super.visitStatement(stmt)
    }
  }

  override def visitReporterApp(app: ReporterApp): Unit = {
    app.reporter match {
      case l: _letvariable =>
        cAssert(
          currentLet.isEmpty || (currentLet.get ne l.let),
          I18N.errors.getN("compiler.LetVariable.notDefined", l.token.text.toUpperCase),
          l.token)
      case _ =>
    }
    super.visitReporterApp(app)
  }
}
