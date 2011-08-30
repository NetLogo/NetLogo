package org.nlogo.prim

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Reporter }

class _lambdavariable(val varNumber: Int) extends Reporter {
  override def toString = super.toString + ":" + varNumber
  override def syntax = Syntax.reporterSyntax(Syntax.WildcardType)
  override def report(context: Context) =
    // LambdaVisitor compiles us out of existence
    throw new IllegalStateException
}
