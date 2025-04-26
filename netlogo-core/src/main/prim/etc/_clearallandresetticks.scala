// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.nvm.{ Command, Context }

class _clearallandresetticks extends Command {
  switches = true
  // true here because resetTicks calls other code
  override def callsOtherCode = true
  override def perform(context: Context): Unit = {
    workspace.clearAll()
    workspace.resetTicks(context)
    context.ip = next
  }
}
