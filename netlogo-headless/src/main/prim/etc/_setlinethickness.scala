// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.nvm.{ Command, Context }

class _setlinethickness extends Command {
  switches = true
  override def perform(context: Context): Unit = {
    world.setLineThickness(
      context.agent,
      argEvalDoubleValue(context, 0))
    context.ip = next
  }
}
