package view25d.prims

import org.nlogo.api.{ Argument, Command, Context }
import org.nlogo.core.Syntax

import view25d.View25DExtension

class UpdateAllViews extends Command {
  override def getSyntax: Syntax =
    Syntax.commandSyntax(agentClassString = "O---")

  override def perform(args: Array[Argument], context: Context): Unit = {
    View25DExtension.patchWindowMap.values.foreach(_.manuallyRefreshReporterView(context))
  }
}
