// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.prim.{ _commandlambda, _letvariable, _reporterlambda }
import org.nlogo.core.{ AstFolder, AstTransformer, Let, ReporterApp }

class ClosureTagger extends AstTransformer {
  val finder = new LetFinder()
  override def visitReporterApp(app: ReporterApp): ReporterApp = {
    app.reporter match {
      case r: _reporterlambda =>
        val newExp = super.visitReporterApp(app)
        val allLets = finder.visitExpression(newExp.args(0))(Seq())
        app.copy(reporter = r.copy(closedLets = allLets))
      case c: _commandlambda =>
        val newExp = super.visitReporterApp(app)
        val allLets = finder.visitExpression(newExp.args(0))(Seq())
        app.copy(reporter = c.copy(closedLets = allLets))
      case _ => super.visitReporterApp(app)
    }
  }
}

// assumes all child lambdas have already been tagged
class LetFinder extends AstFolder[Seq[Let]] {
  override def visitReporterApp(app: ReporterApp)(implicit lets: Seq[Let]): Seq[Let] = {
    app.reporter match {
      case _letvariable(let)                   => lets :+ let
      case _reporterlambda(_, _, reporterLets) => lets ++ reporterLets
      case _commandlambda(_, _, reporterLets)  => lets ++ reporterLets
      case _                                   => super.visitReporterApp(app)
    }
  }
}
