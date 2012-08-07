// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Reporter }

class _randompxcor extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.NumberType)
  override def report(context: Context) =
    Double.box(report_1(context))
  def report_1(context: Context): Double = {
    val min = world.minPxcor
    val max = world.maxPxcor
    min + context.job.random.nextInt(max - min + 1)
  }
}
