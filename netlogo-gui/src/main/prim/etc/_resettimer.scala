// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.nvm.{ Command, Context }

class _resettimer extends Command {

  override def perform(context: Context): Unit = {
    workspace.world.timer.reset()
    context.ip = next
  }
}
