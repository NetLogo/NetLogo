// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ Syntax, OutputDestination }
import org.nlogo.nvm.{ Command, Context }

class _pwd extends Command {
  override def syntax =
    Syntax.commandSyntax("O---", false)
  override def perform(context: Context) {
    val path =
      Option(workspace.getModelPath)
        .getOrElse("no model loaded!")
    workspace.outputObject(path, null, true, true,
      OutputDestination.Normal)
    context.ip = next
  }
}
