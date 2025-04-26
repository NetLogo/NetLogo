// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.OutputDestination
import org.nlogo.nvm.{ Command, Context }

class _outputtype extends Command {

  override def perform(context: Context): Unit = {
    workspace.outputObject(
      args(0).report(context), null, false, false,
      OutputDestination.OutputArea)
    context.ip = next
  }
}
