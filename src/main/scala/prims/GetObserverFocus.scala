package view25d.prims

import org.nlogo.api.{ Argument, Context, Reporter }
import org.nlogo.core.{ LogoList, Syntax }

import view25d.View25DExtension

class GetObserverFocus extends Reporter {
  override def getSyntax: Syntax = {
    Syntax.reporterSyntax(
      right = List(Syntax.StringType), ret = Syntax.ListType, agentClassString = "OTPL",
      blockAgentClassString = Some("?"))
  }

  override def report(args: Array[Argument], context: Context): LogoList = {
    val title = args(0).getString

    LogoList(
      if (View25DExtension.turtleWindowMap.contains(title))
        View25DExtension.turtleWindowMap(title).getObserverPerspectiveFocusPoint
      else if (View25DExtension.patchWindowMap.contains(title))
        View25DExtension.patchWindowMap(title).getObserverPerspectiveFocusPoint
      else
        Array(0.0, 0.0)
    )
  }
}
