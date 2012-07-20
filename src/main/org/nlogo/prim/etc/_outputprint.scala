// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ Syntax, OutputDestination }
import org.nlogo.nvm.{ Command, Context }

class _outputprint extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.WildcardType))
  override def perform(context: Context) {
    workspace.outputObject(
      args(0).report(context), null, true, false,
      OutputDestination.OutputArea)
    context.ip = next
  }
}
