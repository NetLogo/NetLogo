// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.Turtle
import org.nlogo.nvm.{ Command, Context }

class _left extends Command {
  switches = true
  override def perform(context: Context): Unit = {
    perform_1(context, argEvalDoubleValue(context, 0))
  }
  def perform_1(context: Context, delta: Double): Unit = {
    context.agent.asInstanceOf[Turtle].turnRight(-delta)
    context.ip = next
  }
}
