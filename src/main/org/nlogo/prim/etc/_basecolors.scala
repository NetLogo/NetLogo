// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ LogoList, LogoListBuilder, Syntax }
import org.nlogo.nvm.{ Context, Reporter }

class _basecolors extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(Syntax.ListType)

  override def report(context: Context) =
    report_1(context)
  def report_1(context: Context): LogoList =
    _basecolors.cached

}

object _basecolors {
  val cached = LogoList(
    (0 to 13).map(n => Double.box(n * 10 + 5)): _*)
}
