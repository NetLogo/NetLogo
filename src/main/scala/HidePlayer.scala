package org.nlogo.extensions.vid

import org.nlogo.api.{ Argument, Command, Context }
import org.nlogo.core.Syntax

class HidePlayer(player: Player) extends Command {
  override def getSyntax = Syntax.commandSyntax(List())

  override def perform(args: Array[Argument], context: Context): Unit = {
    player.hide()
  }
}
