// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.nvm.{ Command, Context }

class _resetticks extends Command {
  switches = true
  override def callsOtherCode = true
  override def perform(context: Context) {
    workspace.resetTicks(context)
    context.ip = next
  }
}
