// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Reporter}

class _randomconst(n: Long) extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.NumberType)
  override def toString =
    super.toString + ":" + n
  override def report(context: Context) =
    Double.box(report_1(context))
  def report_1(context: Context): Double =
    context.job.random.nextLong(n)
}
