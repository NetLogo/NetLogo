// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.LogoListBuilder
import org.nlogo.core.{ I18N, LogoList }
import org.nlogo.nvm.{ Context, EngineException, Reporter, Task }

class _nvalues extends Reporter {

  override def report(context: Context): LogoList = {
    // get the first argument...
    val n = argEvalIntValue(context, 0)
    if (n < 0)
      throw new EngineException( context, this,
        I18N.errors.getN("org.nlogo.prim.etc.$common.noNegativeNumber", displayName))
    // make the result list.
    val result = new LogoListBuilder
    val task = argEvalReporterTask(context, 1)
    if (task.syntax.minimum > 1)
      throw new EngineException(context, this, Task.missingInputs(task, 1))
    for (i <- 0 until n)
      result.add(task.report(context, Array[AnyRef](Double.box(i))))
    result.toLogoList
  }

}
