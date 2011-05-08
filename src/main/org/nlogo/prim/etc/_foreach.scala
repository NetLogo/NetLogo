package org.nlogo.prim.etc

import org.nlogo.api.LogoException
import org.nlogo.nvm.{ Command, Context, EngineException, Syntax }

class _foreach extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.TYPE_REPEATABLE | Syntax.TYPE_LIST,
      Syntax.TYPE_COMMAND_LAMBDA), 2) // default # of inputs
  override def perform(context: Context) {
    var size = 0
    val n = args.length - 1
    val iters = for (i <- 0 until n) yield {
      val list = argEvalList(context, i)
      if (i == 0) size = list.size
      else if (size != list.size)
        throw new EngineException(context, this, "All the list arguments to FOREACH must be the same length.")
      list.iterator
    }
    val lambda = argEvalCommandLambda(context, n)
    if(n < lambda.formals.size)
      throw new EngineException(
        context, this, lambda.missingInputs(n))
    var i = 0
    val actuals = new Array[AnyRef](n)
    while(i < size && !context.finished) {
      var j = 0
      while(j < n) {
        actuals(j) = iters(j).next()
        j += 1
      }
      lambda.perform(context, actuals)
      i += 1
    }
    context.ip = next
  }
}
