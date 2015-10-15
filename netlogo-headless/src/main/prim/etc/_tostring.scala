// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Pure
import org.nlogo.nvm.{ Context, Reporter }

class _tostring extends Reporter with Pure {
  override def report(context: Context): String =
    report_1(context, args(0).report(context))
  def report_1(context: Context, arg0: AnyRef): String =
    arg0.toString
}
