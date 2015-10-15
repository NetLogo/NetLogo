// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Pure
import org.nlogo.nvm.{ Context, Reporter }

@annotation.strictfp
class _mult extends Reporter with Pure {

  override def report(context: Context): java.lang.Double =
    Double.box(
      report_1(context,
        argEvalDoubleValue(context, 0),
        argEvalDoubleValue(context, 1)))

  def report_1(context: Context, arg0: Double, arg1: Double): Double =
    arg0 * arg1

}
