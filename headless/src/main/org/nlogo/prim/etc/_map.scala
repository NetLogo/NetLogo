// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ LogoListBuilder, Syntax }
import org.nlogo.nvm.{ EngineException, Context, Reporter }

class _map extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(Array(Syntax.ReporterTaskType, Syntax.RepeatableType | Syntax.ListType),
      Syntax.ListType,
      2) // default # of inputs

  // Oh boy, this is going to be really fun one to generate...
  //   ~Forrest (7/21/2006)
  // Perhaps we should have separate primitives for the variadic and
  // non-variadic cases and only attempt to generate the non-variadic
  // case. - ST 3/20/08
  override def report(context: Context) = {

    val task = argEvalReporterTask(context, 0)
    val n = args.length - 1
    if(n < task.formals.size)
      throw new EngineException(
        context, this, task.missingInputs(n))

    // get all of the list args, if any.
    var size = 0
    val iters = for (i <- 1 to n) yield {
      val list = argEvalList(context, i)
      if (i == 1) size = list.size
      else if (size != list.size)
        throw new EngineException(context, this, "All the list arguments to MAP must be the same length.")
      list.iterator
    }

    // make the result list.
    val result = new LogoListBuilder
    var i = 0
    val actuals = new Array[AnyRef](n)
    while(i < size) {
      var j = 0
      while(j < n) {
        actuals(j) = iters(j).next()
        j += 1
      }
      result.add(task.report(context, actuals))
      i += 1
    }
    result.toLogoList
  }
}
