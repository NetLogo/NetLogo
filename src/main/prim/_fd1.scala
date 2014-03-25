// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.{ api, agent }
import org.nlogo.nvm.{ Command, Context }

class _fd1 extends Command {
  override def syntax =
    api.Syntax.commandSyntax("-T--", true)
  override def perform(context: Context) {
    perform_1(context)
  }
  def perform_1(context: Context) {
    try context.agent.asInstanceOf[agent.Turtle].jump(1)
    catch { case e: api.AgentException => } // ignore
    context.ip = next
  }
}
