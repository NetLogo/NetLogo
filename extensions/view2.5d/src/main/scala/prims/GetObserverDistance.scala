package view25d.prims

import java.lang.Double

import org.nlogo.api.{ Argument, Context, Reporter }
import org.nlogo.core.Syntax

import view25d.View25DExtension

class GetObserverDistance extends Reporter {
  override def getSyntax: Syntax =
    Syntax.reporterSyntax(right = List(Syntax.StringType), ret = Syntax.NumberType)

  override def report(args: Array[Argument], context: Context): Double = {
    val title = args(0).getString

    if (View25DExtension.turtleWindowMap.contains(title))
      View25DExtension.turtleWindowMap(title).getObserverDistance
    else if (View25DExtension.patchWindowMap.contains(title))
      View25DExtension.patchWindowMap(title).getObserverDistance
    else
      0
  }
}
