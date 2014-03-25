// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ I18N, LogoList, LogoListBuilder, Syntax }
import org.nlogo.nvm.{ Context, EngineException, Reporter }

class _nvalues extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(Array(Syntax.NumberType, Syntax.ReporterTaskType),
                          Syntax.ListType)

  override def report(context: Context): LogoList = {
    // get the first argument...
    val n = argEvalIntValue(context, 0)
    if (n < 0)
      throw new EngineException( context, this,
        I18N.errors.getN("org.nlogo.prim.etc.$common.noNegativeNumber", displayName))
    // make the result list.
    val result = new LogoListBuilder
    val task = argEvalReporterTask(context, 1)
    if(task.formals.size > 1)
      throw new EngineException(
        context, this, task.missingInputs(1))
    for (i <- 0 until n)
      result.add(task.report(context, Array[AnyRef](Double.box(i))))
    result.toLogoList
  }

}
