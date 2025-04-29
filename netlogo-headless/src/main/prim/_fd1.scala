// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.nvm.{ Command, Context }
import org.nlogo.{ agent, api }

class _fd1 extends Command {
  switches = true
  override def perform(context: Context): Unit = {
    perform_1(context)
  }
  def perform_1(context: Context): Unit = {
    try context.agent.asInstanceOf[agent.Turtle].jump(1)
    catch { case e: api.AgentException => } // ignore
    context.ip = next
  }
}
