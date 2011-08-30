package org.nlogo.prim.etc

import org.nlogo.api.{ LogoListBuilder, Syntax }
import org.nlogo.nvm.{ ArgumentTypeException, Context, EngineException, Reporter }

class _filter extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(Array(Syntax.ReporterTaskType, Syntax.ListType),
      Syntax.ListType)

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
            context, this, 0, Syntax.BooleanType, obj)
      }
    val result = builder.toLogoList
    if (result.size == list.size) list
    else result
  }

}
