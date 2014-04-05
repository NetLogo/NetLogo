// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ Syntax, SyntaxJ }
import org.nlogo.nvm.{ Command, Context }

class _clearturtles extends Command {
  override def syntax =
    SyntaxJ.commandSyntax("O---", true)
  override def perform(context: Context) {
    world.clearTurtles()
    context.ip = next
  }
}
