// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ LogoList, Syntax }
import org.nlogo.nvm.{ Context, Pure, Reporter }

class _fput extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      Array(Syntax.WildcardType, Syntax.ListType),
      Syntax.ListType)
  override def report(context: Context): LogoList =
    report_1(context, args(0).report(context), argEvalList(context, 1))
  def report_1(context: Context, obj: AnyRef, list: LogoList): LogoList =
    list.fput(obj)
}
