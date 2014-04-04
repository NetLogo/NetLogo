// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.nvm.{ Command, Context }

class _reloadextensions extends Command {
  override def perform(context: Context) {
    workspace.getExtensionManager.reset()
  }
}
