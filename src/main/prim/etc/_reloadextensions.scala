// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ Syntax, SyntaxJ }
import org.nlogo.nvm.{ Command, Context }

class _reloadextensions extends Command {
  override def syntax =
    SyntaxJ.commandSyntax("OTPL", true)
  override def perform(context: Context) {
    workspace.getExtensionManager.reset()
  }
}
