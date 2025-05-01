// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.nvm.{ Command, Context }

class _settopology extends Command {
  override def perform(context: Context): Unit = {
    workspace.changeTopology(
      argEvalBooleanValue(context, 0),
      argEvalBooleanValue(context, 1))
    context.ip = next
  }
}
