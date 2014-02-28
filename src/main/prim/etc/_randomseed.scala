// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ Dump, Syntax }
import org.nlogo.nvm.{ Command, Context, EngineException }

class _randomseed extends Command {

  override def syntax =
    Syntax.commandSyntax(Array(Syntax.NumberType))

  override def perform(context: Context) {
    perform_1(context, argEvalDoubleValue(context, 0))
  }

  def perform_1(context: Context, arg0: Double) {
    val l = arg0.toLong
    if (l < -2147483648 || l > 2147483647)
      throw new EngineException(
        context, this,
        Dump.number(arg0) + " is not in the allowable range for random seeds (-2147483648 to 2147483647)")
    context.job.random.setSeed(l.toInt)
    context.ip = next
  }

}
