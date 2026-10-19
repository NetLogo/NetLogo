package view25d.prims

import org.nlogo.api.{ Argument, Command, Context }
import org.nlogo.core.Syntax

import view25d.View25DExtension

class SetTurtleStemColor extends Command {
  override def getSyntax: Syntax =
    Syntax.commandSyntax(right = List(Syntax.StringType, Syntax.ReporterType), agentClassString = "O---")

  override def perform(args: Array[Argument], context: Context): Unit = {
    View25DExtension.turtleWindowMap.get(args(0).getString).foreach(window => {
      window.setStemColorReporter(args(1).getReporter)
      // Force update of the Array List
      window.manuallyRefreshReporterView(context)
      window.refresh()
    })
  }
}
