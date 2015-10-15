// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.Pure
import org.nlogo.nvm.{ Context, Reporter }

class _minus extends Reporter with Pure {

  override def report(context: Context): java.lang.Double =
    Double.box(
      report_1(context,
               argEvalDoubleValue(context, 0),
               argEvalDoubleValue(context, 1)))

  def report_1(context: Context, d0: Double, d1: Double): Double =
    d0 - d1

}
