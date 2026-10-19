package view25d.prims

import org.nlogo.api.{ Argument, Command, Context }
import org.nlogo.core.Syntax

import view25d.View25DExtension

class SetTurtleStemThickness extends Command {
  override def getSyntax: Syntax =
    Syntax.commandSyntax(right = List(Syntax.StringType, Syntax.NumberType), agentClassString = "O---")

  override def perform(args: Array[Argument], context: Context): Unit = {
    View25DExtension.turtleWindowMap.get(args(0).getString).foreach(window => {
      window.setStemThickness(args(1).getDoubleValue.max(0).min(1))
      window.refresh()
    })
  }
}
