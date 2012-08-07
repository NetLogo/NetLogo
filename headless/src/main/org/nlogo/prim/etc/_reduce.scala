// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ I18N, Syntax }
import org.nlogo.nvm.{ EngineException, Context, Reporter }

class _reduce extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(Array(Syntax.ReporterTaskType, Syntax.ListType), Syntax.WildcardType)

  override def report(context: Context) = {
    val task = argEvalReporterTask(context, 0)
    if(task.formals.size > 2)
      throw new EngineException(
        context, this, task.missingInputs(2))
    val list = argEvalList(context, 1)
    if (list.size < 1)
      throw new EngineException( context , this , I18N.errors.get("org.nlogo.prim._reduce.emptyListInvalidInput"))
    val it = list.iterator
    var result = it.next()
    while (it.hasNext)
      result = task.report(context, Array(result, it.next()))
    result
  }

}
