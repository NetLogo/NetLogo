// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.nvm.{ Command, Context }

class _foreverbuttonstart extends Command {
  switches = true

  override def perform(context: Context): Unit = {
    if (workspace.world.ticks == -1) {
      context.finished = true
    } else {
      context.ip = next
    }
  }
}
