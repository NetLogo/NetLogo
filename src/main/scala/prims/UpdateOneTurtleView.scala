package view25d.prims

import org.nlogo.api.{ Argument, Command, Context, ExtensionException }
import org.nlogo.core.Syntax

import view25d.View25DExtension

class UpdateOneTurtleView extends Command {
  override def getSyntax: Syntax =
    Syntax.commandSyntax(right = List(Syntax.StringType, Syntax.AgentsetType), agentClassString = "O---")

  override def perform(args: Array[Argument], context: Context): Unit = {
    val title = args(0).getString

    View25DExtension.turtleWindowMap.get(title) match {
      case Some(window) =>
        window.setAgentSet(args(1).getAgentSet)
        window.manuallyRefreshReporterView(context)
      case None => throw new ExtensionException(s"No Variable Visualization window with title $title")
    }
  }
}

