package view25d.prims

import org.nlogo.api.{ Argument, Command, Context }
import org.nlogo.core.Syntax

import view25d.View25DExtension

class DecoratePatchView(on: Boolean) extends Command {
  override def getSyntax: Syntax =
    Syntax.commandSyntax(right = List(Syntax.StringType))

  override def perform(args: Array[Argument], context: Context): Unit = {
    View25DExtension.patchWindowMap.get(args(0).getString).foreach(window => {
      window.doingTurtles = on
      window.manuallyRefreshReporterView(context)
    })
  }
}
