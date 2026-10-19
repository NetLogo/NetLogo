package view25d.prims

import org.nlogo.api.{ Argument, Context, Reporter }
import org.nlogo.core.{ LogoList, Syntax }

import view25d.View25DExtension

class GetObserverAngles extends Reporter {
  override def getSyntax: Syntax =
    Syntax.reporterSyntax(right = List(Syntax.StringType), ret = Syntax.ListType)

  override def report(args: Array[Argument], context: Context): LogoList = {
    val title = args(0).getString

    LogoList(
      if (View25DExtension.turtleWindowMap.contains(title))
        View25DExtension.turtleWindowMap(title).getObserverPerspectiveAngles
      else if (View25DExtension.patchWindowMap.contains(title))
        View25DExtension.patchWindowMap(title).getObserverPerspectiveAngles
      else
        Array(0.0, 0.0)
    )
  }
}
