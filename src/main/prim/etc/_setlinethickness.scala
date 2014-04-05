// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ Syntax, SyntaxJ }
import org.nlogo.nvm.{ Command, Context }

class _setlinethickness extends Command {
  override def syntax =
    SyntaxJ.commandSyntax(
      Array(Syntax.NumberType),
      "-T--", switches = true)
  override def perform(context: Context) {
    world.setLineThickness(
      context.agent,
      argEvalDoubleValue(context, 0))
    context.ip = next
  }
}
