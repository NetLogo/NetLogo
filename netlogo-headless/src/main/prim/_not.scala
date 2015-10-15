// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.Pure
import org.nlogo.nvm.{ Context, Reporter }

class _not extends Reporter with Pure {

  override def report(context: Context): java.lang.Boolean =
    Boolean.box(report_1(context, argEvalBooleanValue(context, 0)))

  def report_1(context: Context, arg0: Boolean): Boolean =
    !arg0

}
