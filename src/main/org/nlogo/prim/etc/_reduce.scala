package org.nlogo.prim.etc

import org.nlogo.nvm.{ EngineException, Context, Reporter, Syntax }

class _reduce extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(Array(Syntax.TYPE_REPORTER_LAMBDA, Syntax.TYPE_LIST), Syntax.TYPE_WILDCARD)

  override def report(context: Context) = {
    val lambda = argEvalReporterLambda(context, 0)
    if(lambda.formals.size > 2)
      throw new EngineException(
        context, this, lambda.missingInputs(2))
    val list = argEvalList(context, 1)
    if (list.size < 1)
      throw new EngineException(context, this, "The list argument to 'reduce' must not be empty.")
    val it = list.iterator
    var result = it.next()
    while (it.hasNext)
      result = lambda.report(context, Array(result, it.next()))
    result
  }

}
