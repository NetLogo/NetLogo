// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.prim.{ _commandlambda, _lambdavariable, _letvariable, _reporterlambda, Lambda }
import org.nlogo.core.{ AstFolder, AstTransformer, ClosedLet, ClosedLambdaVariable, ClosedVariable, ReporterApp }

class ClosureTagger extends AstTransformer {
  override def visitReporterApp(app: ReporterApp): ReporterApp = {
    app.reporter match {
      case r: _reporterlambda =>
        val newExp = super.visitReporterApp(app)
        val finder = new ClosedVariableFinder(r.argumentNames)
        val allClosedVariables = finder.visitExpression(newExp.args(0))(using Set())
        newExp.copy(reporter = r.copy(closedVariables = allClosedVariables))
      case c: _commandlambda =>
        val newExp = super.visitReporterApp(app)
        val finder = new ClosedVariableFinder(c.argumentNames)
        val allClosedVariables = finder.visitExpression(newExp.args(0))(using Set())
        newExp.copy(reporter = c.copy(closedVariables = allClosedVariables))
      case _ => super.visitReporterApp(app)
    }
  }
}

// assumes all child lambdas have already been tagged
class ClosedVariableFinder(lambdaVarNames: Seq[String]) extends AstFolder[Set[ClosedVariable]] {
  override def visitReporterApp(app: ReporterApp)(implicit closedVars: Set[ClosedVariable]): Set[ClosedVariable] = {
    app.reporter match {
      case _letvariable(let)          => closedVars + ClosedLet(let)
      case _lambdavariable(name, _) if ! lambdaVarNames.contains(name) =>
        closedVars + ClosedLambdaVariable(name)
      case Lambda(_, _, reporterLets) => closedVars ++ reporterLets
      case _                          => super.visitReporterApp(app)
    }
  }
}
