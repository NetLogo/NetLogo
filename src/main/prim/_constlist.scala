// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.Syntax
import org.nlogo.api.{ Dump, LogoList }
import org.nlogo.nvm.{ Reporter, Pure, Context }

class _constlist(value: LogoList) extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.ListType)
  override def toString =
    super.toString + ":" + Dump.logoObject(value)
  override def report(context: Context): LogoList =
    report_1(context)
  def report_1(context: Context): LogoList =
    value
}
