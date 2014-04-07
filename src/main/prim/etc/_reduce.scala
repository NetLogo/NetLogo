// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Syntax
import org.nlogo.api.I18N
import org.nlogo.nvm.{ EngineException, Context, Reporter }

class _reduce extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.ReporterTaskType, Syntax.ListType),
      ret = Syntax.WildcardType)

  override def report(context: Context): AnyRef = {
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
