// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ LogoListBuilder}
import org.nlogo.core.Syntax
import org.nlogo.core.LogoList
import org.nlogo.nvm.{ AnonymousProcedure, ArgumentTypeException, Context, Reporter }
import org.nlogo.nvm.RuntimePrimitiveException

class _filter extends Reporter {
  def report(context: Context): LogoList = {
    val reporter = argEvalAnonymousReporter(context, 0)
    val list = argEvalList(context, 1)
    if (reporter.syntax.minimum > 1)
      throw new RuntimePrimitiveException(context, this, AnonymousProcedure.missingInputs(reporter, 1))
    val builder = new LogoListBuilder
    for (item <- list)
      reporter.report(context, Array(item)) match {
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
