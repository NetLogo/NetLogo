// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.nvm.{ Command, Context }

class _display extends Command {
  switches = true
  override def perform(context: Context) {
    workspace.enablePeriodicRendering()
    workspace.requestDisplayUpdate(true)
    context.ip = next
  }
}
