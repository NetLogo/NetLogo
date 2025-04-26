// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.OutputDestination
import org.nlogo.nvm.{ Command, Context }

class _type extends Command {

  override def perform(context: Context): Unit = {
    workspace.outputObject(
      args(0).report(context), null, false, false,
      OutputDestination.Normal)
    context.ip = next
  }
}
