// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.Link
import org.nlogo.nvm.{ Command, Context }

class _untie extends Command {
  switches = true
  override def perform(context: Context) {
    context.agent.asInstanceOf[Link].mode(Link.MODE_NONE)
    context.ip = next
  }
}
