// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ Syntax, SyntaxJ }
import org.nlogo.nvm.{ Command, Context }

class _setcurdir extends Command {
  override def syntax =
    SyntaxJ.commandSyntax(Array(Syntax.StringType))
  override def perform(context: Context) {
    workspace.fileManager.setPrefix(argEvalString(context, 0))
    context.ip = next
  }
}
