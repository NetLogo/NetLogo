// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.Turtle
import org.nlogo.nvm.{ Command, Context }

class _home extends Command {


  switches = true
  override def perform(context: Context): Unit = {
    context.agent.asInstanceOf[Turtle].home()
    context.ip = next
  }
}
