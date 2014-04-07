// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ Syntax }
import org.nlogo.nvm.{ Context, Reporter }

class _behaviorspacerunnumber extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.NumberType)
  override def report(context: Context): java.lang.Double =
    Double.box(report_1(context))
  def report_1(context: Context): Double =
    workspace.behaviorSpaceRunNumber
}
