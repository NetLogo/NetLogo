package org.nlogo.prim.etc

import org.nlogo.nvm.{ EngineException, Context, Reporter, Syntax }
import org.nlogo.api.I18N

class _reduce extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(Array(Syntax.TYPE_REPORTER_TASK, Syntax.TYPE_LIST), Syntax.TYPE_WILDCARD)

  override def report(context: Context) = {
    val lambda = argEvalReporterLambda(context, 0)
    if(lambda.formals.size > 2)
      throw new EngineException(
        context, this, lambda.missingInputs(2))
    val list = argEvalList(context, 1)
    if (list.size < 1)
      throw new EngineException( context , this , I18N.errors.get("org.nlogo.prim._reduce.emptyListInvalidInput"))
    val it = list.iterator
    var result = it.next()
    while (it.hasNext)
      result = lambda.report(context, Array(result, it.next()))
    result
  }

}
