package view25d.prims

import org.nlogo.api.{ Argument, Command, Context }
import org.nlogo.core.Syntax

import view25d.View25DExtension

class SetObserverFocus extends Command {
  override def getSyntax: Syntax =
    Syntax.commandSyntax(right = List(Syntax.StringType, Syntax.NumberType, Syntax.NumberType),
                         agentClassString = "O---")

  override def perform(args: Array[Argument], context: Context): Unit = {
    val title = args(0).getString
    val x = args(1).getDoubleValue
    val y = args(2).getDoubleValue

    View25DExtension.patchWindowMap.get(title).foreach(window => {
      val coords = window.getObserverPerspectiveFocusPoint

      window.shiftObserverFocusPoint(x - coords(0), y - coords(1))
    })

    View25DExtension.turtleWindowMap.get(title).foreach(window => {
      val coords = window.getObserverPerspectiveFocusPoint

      window.shiftObserverFocusPoint(x - coords(0), y - coords(1))
    })
  }
}
