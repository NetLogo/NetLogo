package view25d.prims

import org.nlogo.api.{ Argument, Command, Context }
import org.nlogo.core.Syntax

import view25d.View25DExtension

class SetLinkDisplayMode(inXYPlane: Boolean) extends Command {
  override def getSyntax: Syntax =
    Syntax.commandSyntax(right = List(Syntax.StringType))

  override def perform(args: Array[Argument], context: Context): Unit = {
    val title = args(0).getString

    View25DExtension.patchWindowMap.get(title).foreach(_.setLinksDisplayMode(inXYPlane))
    View25DExtension.turtleWindowMap.get(title).foreach(_.setLinksDisplayMode(inXYPlane))
  }
}
