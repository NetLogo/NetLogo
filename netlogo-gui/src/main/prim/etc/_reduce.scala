// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Syntax
import org.nlogo.core.I18N
import org.nlogo.nvm.{ AnonymousProcedure, EngineException, Context, Reporter }

class _reduce extends Reporter {

  override def report(context: Context) = {
    val rep = argEvalReporter(context, 0)
    if (rep.syntax.minimum > 2)
      throw new EngineException(context, this, AnonymousProcedure.missingInputs(rep, 2))
    val list = argEvalList(context, 1)
    if (list.size < 1)
      throw new EngineException(context , this , I18N.errors.get("org.nlogo.prim._reduce.emptyListInvalidInput"))
    val it = list.iterator
    var result = it.next()
    while (it.hasNext)
      result = rep.report(context, Array(result, it.next()))
    result
  }

}
