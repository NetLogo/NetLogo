package view25d.prims

import org.nlogo.api.{ Argument, Command, Context, ExtensionException }
import org.nlogo.core.Syntax

import view25d.View25DExtension

class SetObserverDistance extends Command {
  private val MinDistance = 1.0
  // private val MaxDistance = 130.0

  override def getSyntax: Syntax =
    Syntax.commandSyntax(right = List(Syntax.StringType, Syntax.NumberType),
                         agentClassString = "O---")

  override def perform(args: Array[Argument], context: Context): Unit = {
    val title = args(0).getString
    val distance = args(1).getDoubleValue

    if (distance < MinDistance)
      throw new ExtensionException(s"Cannot set observer distance below $MinDistance")

    View25DExtension.patchWindowMap.get(title).foreach(window => {
      window.zoomToDistance(distance)
    })

    View25DExtension.turtleWindowMap.get(title).foreach(window => {
      window.zoomToDistance(distance)
    })
  }
}
