package org.nlogo.prim

import org.nlogo.api.LogoException
import org.nlogo.nvm.{ EngineException, Reporter, Syntax, Context }

class _lambdavariable(val varNumber: Int) extends Reporter {
  override def toString = super.toString + ":" + varNumber
  override def syntax = Syntax.reporterSyntax(Syntax.TYPE_WILDCARD)
  override def report(context: Context) =
    // LambdaVisitor compiles us out of existence
    throw new IllegalStateException
}
