// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.Link
import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Reporter }

class _linklength extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.NumberType, "---L")
  override def report(context: Context): java.lang.Double =
    Double.box(report_1(context))
  def report_1(context: Context): Double =
    context.agent.asInstanceOf[Link].size
}
