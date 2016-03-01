// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.nvm.{ Command, Context }
import org.nlogo.api.LogoException
import org.nlogo.core.Syntax

class _magicopen(_name: Option[String]) extends Command {
  private[this] val name = _name

  switches = true

  def syntax: Syntax = {
    Syntax.commandSyntax(
      right = List(Syntax.StringType | Syntax.RepeatableType),
      agentClassString = "O---",
      defaultOption = Some(1),
      minimumOption = Some(0));
  }

  override def perform(context: Context): Unit = {
    val openName = name.getOrElse(argEvalString(context, 0))
    workspace.magicOpen(openName)
    context.ip = next
  }

}
