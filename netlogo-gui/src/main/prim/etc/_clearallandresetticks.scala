// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

// true here because resetTicks calls other code
import org.nlogo.nvm.{ Command, Context }

class _clearallandresetticks extends Command {


  switches = true
  override def callsOtherCode = true
  override def perform(context: Context) {
    workspace.clearAll()
    workspace.resetTicks(context)
    context.ip = next
  }
}
