// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.Dump
import org.nlogo.core.{ LogoList, Pure }
import org.nlogo.nvm.{ Context, Reporter }

class _constlist(value: LogoList) extends Reporter with Pure {
  override def toString =
    super.toString + ":" + Dump.logoObject(value)
  override def report(context: Context): LogoList =
    report_1(context)
  def report_1(context: Context): LogoList =
    value
}
