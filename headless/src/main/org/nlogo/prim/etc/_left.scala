// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.Turtle
import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Command, Context }

class _left extends Command {
  override def syntax =
    Syntax.commandSyntax(
      Array(Syntax.NumberType), "-T--", true)
  override def perform(context: Context) {
    perform_1(context, argEvalDoubleValue(context, 0));
  }
  def perform_1(context: Context, delta: Double) {
    context.agent.asInstanceOf[Turtle].turnRight(-delta)
    context.ip = next
  }
}
