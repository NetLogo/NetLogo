// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.nvm.{ Command, Context }

class _clearglobals extends Command {
  switches = true
  override def perform(context: Context) {
    workspace.world.clearGlobals()
    context.ip = next
  }
}
