// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ I18N, Syntax }
import org.nlogo.nvm.{ Command, Context, EngineException, NonLocalExit, Procedure }

class _foreach extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.RepeatableType | Syntax.ListType,
      Syntax.CommandTaskType), 2) // default # of inputs
  override def perform(context: Context) {
    var size = 0
    val n = args.length - 1
    val iters = for (i <- 0 until n) yield {
      val list = argEvalList(context, i)
      if (i == 0) size = list.size
      else if (size != list.size)
        throw new EngineException(context, this,
          I18N.errors.get("org.nlogo.prim.etc._foreach.listsMustBeSameLength"))
      list.iterator
    }
    val task = argEvalCommandTask(context, n)
    if(n < task.formals.size)
      throw new EngineException(
        context, this, task.missingInputs(n))
    var i = 0
    val actuals = new Array[AnyRef](n)
    try {
      while(i < size && !context.finished) {
        var j = 0
        while(j < n) {
          actuals(j) = iters(j).next()
          j += 1
        }
        task.perform(context, actuals)
        i += 1
      }
      context.ip = next
    }
    catch {
      case NonLocalExit if context.activation.procedure.tpe == Procedure.Type.COMMAND =>
        context.stop()
    }
  }
}
