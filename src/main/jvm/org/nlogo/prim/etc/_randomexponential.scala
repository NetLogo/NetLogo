// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Reporter }

class _randomexponential extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      Array(Syntax.NumberType),
      Syntax.NumberType)
  override def report(context: Context) =
    Double.box(report_1(context, argEvalDoubleValue(context, 0)))
  def report_1(context: Context, d: Double) =
    validDouble(-d * StrictMath.log(context.job.random.nextDouble))
}
