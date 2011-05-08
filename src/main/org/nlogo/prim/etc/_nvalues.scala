package org.nlogo.prim.etc

import org.nlogo.api.LogoListBuilder
import org.nlogo.nvm.{Context, EngineException, Reporter, Syntax}

class _nvalues extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(Array(Syntax.TYPE_NUMBER, Syntax.TYPE_REPORTER_LAMBDA),
                          Syntax.TYPE_LIST)

  override def report(context: Context) = {
    // get the first argument...
    val n = argEvalIntValue(context, 0)
    if (n < 0)
      throw new EngineException(
        context, this, displayName + " cannot take a negative number")
    // make the result list.
    val result = new LogoListBuilder
    val lambda = argEvalReporterLambda(context, 1)
    if(lambda.formals.size > 1)
      throw new EngineException(
        context, this, lambda.missingInputs(1))
    for (i <- 0 until n)
      result.add(lambda.report(context, Array(java.lang.Double.valueOf(i))))
    result.toLogoList
  }

}
