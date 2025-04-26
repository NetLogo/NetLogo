// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.OutputDestination
import org.nlogo.nvm.{ Command, Context }

class _outputshow extends Command {

  override def perform(context: Context): Unit = {
    workspace.outputObject(
      args(0).report(context), context.agent,
      true, true, OutputDestination.OutputArea)
    context.ip = next
  }
}
