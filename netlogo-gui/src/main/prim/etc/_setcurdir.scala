// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.nvm.{ Command, Context }

class _setcurdir extends Command {

  override def perform(context: Context): Unit = {
    workspace.fileManager.setPrefix(argEvalString(context, 0))
    context.ip = next
  }
}
