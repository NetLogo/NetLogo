// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.{ Dump, LogoList, Syntax }
import org.nlogo.nvm.{ Reporter, Pure, Context }

class _constlist(value: LogoList) extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(Syntax.ListType)
  override def toString =
    super.toString + ":" + Dump.logoObject(value)
  override def report(context: Context) =
    report_1(context)
  def report_1(context: Context) =
    value
}
