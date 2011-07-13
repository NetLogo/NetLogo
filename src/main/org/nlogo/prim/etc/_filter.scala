package org.nlogo.prim.etc

import org.nlogo.api.LogoListBuilder
import org.nlogo.nvm.{ ArgumentTypeException, Context, EngineException, Reporter, Syntax }

class _filter extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(Array(Syntax.TYPE_REPORTER_TASK, Syntax.TYPE_LIST),
      Syntax.TYPE_LIST)

  def report(context: Context) = {
    val lambda = argEvalReporterLambda(context, 0)
    val list = argEvalList(context, 1)
    if(lambda.formals.size > 1)
      throw new EngineException(
        context, this, lambda.missingInputs(1))
    val builder = new LogoListBuilder
    for (item <- list)
      lambda.report(context, Array(item)) match {
        case b: java.lang.Boolean =>
          if (b.booleanValue)
            builder.add(item)
        case obj =>
          throw new ArgumentTypeException(
            context, this, 0, Syntax.TYPE_BOOLEAN, obj)
      }
    val result = builder.toLogoList
    if (result.size == list.size) list
    else result
  }

}
