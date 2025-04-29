// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.nvm.{ Command, Context }

class _resetperspective extends Command {
  switches = true
  override def perform(context: Context): Unit = {
    world.observer.resetPerspective()
    context.ip = next
  }
}
