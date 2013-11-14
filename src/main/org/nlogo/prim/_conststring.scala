// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Reporter, Pure, Context }

class _conststring(value: String) extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(Syntax.StringType)
  override def toString =
    super.toString + ":\"" + value + "\""
  override def report(context: Context): String =
    report_1(context)
  def report_1(context: Context): String =
    value
}
