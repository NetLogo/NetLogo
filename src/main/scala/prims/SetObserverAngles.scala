package view25d.prims

import org.nlogo.api.{ Argument, Command, Context }
import org.nlogo.core.Syntax

import view25d.View25DExtension

class SetObserverAngles extends Command {
  override def getSyntax: Syntax =
    Syntax.commandSyntax(right = List(Syntax.StringType, Syntax.NumberType, Syntax.NumberType),
                         agentClassString = "O---")

  override def perform(args: Array[Argument], context: Context): Unit = {
    val title = args(0).getString
    val heading = args(1).getDoubleValue
    val pitch = args(2).getDoubleValue

    View25DExtension.patchWindowMap.get(title).foreach(window => {
      val angles = window.getObserverPerspectiveAngles

      window.updateObserverPerspectiveAnglesWithDeltas(heading - angles(0), angles(1) - pitch)
    })

    View25DExtension.turtleWindowMap.get(title).foreach(window => {
      val angles = window.getObserverPerspectiveAngles

      window.updateObserverPerspectiveAnglesWithDeltas(heading - angles(0), angles(1) - pitch)
    })
  }
}
