// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.Turtle
import org.nlogo.api.{ Syntax, AgentException }
import org.nlogo.nvm.{ Command, Context }

class _jump extends Command {
  override def syntax =
    Syntax.commandSyntax(
      Array(Syntax.NumberType), "-T--", true)
  override def perform(context: Context) {
    perform_1(context, argEvalDoubleValue(context, 0))
  }
  def perform_1(context: Context, distance: Double) {
    try context.agent.asInstanceOf[Turtle].jump(distance)
    catch {
      case e: AgentException => // ignore
    }
    context.ip = next
  }
}
