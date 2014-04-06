// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Syntax
import org.nlogo.nvm.{ Context, Pure, Reporter }

class _tostring extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.WildcardType),
      ret = Syntax.StringType)
  override def report(context: Context): String =
    report_1(context, args(0).report(context))
  def report_1(context: Context, arg0: AnyRef): String =
    arg0.toString
}
