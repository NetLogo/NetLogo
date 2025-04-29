// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.nvm.{ Command, Context }

class _tick extends Command {


  switches = true
  override def callsOtherCode = true
  override def perform(context: Context): Unit = {
    workspace.tick(context, this)
    context.ip = next
  }
}
