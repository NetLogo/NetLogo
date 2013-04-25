// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Command, Context }

class _goto extends Command {
  override def syntax =
    Syntax.commandSyntax
  override def toString =
    super.toString + ":" + offset
  override def perform(context: Context) {
    perform_1(context)
  }
  def perform_1(context: Context) {
    context.ip = offset
  }
}
