package view25d.prims

import org.nlogo.api.{ Argument, Command, Context }
import org.nlogo.core.Syntax

import view25d.View25DExtension

class SetZScale extends Command {
  override def getSyntax: Syntax =
    Syntax.commandSyntax(right = List(Syntax.StringType, Syntax.NumberType), agentClassString = "O---")

  override def perform(args: Array[Argument], context: Context): Unit = {
    val title = args(0).getString
    val scale = args(1).getDoubleValue

    View25DExtension.patchWindowMap.get(title).foreach(window => {
      window.zoomZby(scale - window.zScale)
    })

    View25DExtension.turtleWindowMap.get(title).foreach(window => {
      window.zoomZby(scale - window.zScale)
    })
  }
}
