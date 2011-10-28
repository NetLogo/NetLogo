// (C) 2011 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Command, Context }
class _randomseed extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.NumberType))
  override def perform(context: Context) {
    perform_1(context, argEvalDoubleValue(context, 0))
  }
  def perform_1(context: Context, arg0: Double) {
    context.job.random.setSeed(validLong(arg0))
    context.ip = next
  }
}
