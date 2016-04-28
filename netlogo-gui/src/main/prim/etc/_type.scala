// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ OutputDestination }
import org.nlogo.core.Syntax
import org.nlogo.nvm.{ Command, Context, Workspace }

class _type extends Command {

  override def perform(context: Context) {
    workspace.outputObject(
      args(0).report(context), null, false, false,
      OutputDestination.Normal)
    context.ip = next
  }
}
