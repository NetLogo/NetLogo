// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.nvm.{ Command, Context }

class _clearturtles extends Command {
  switches = true
  override def perform(context: Context) {
    world.clearTurtles()
    context.ip = next
  }
}
