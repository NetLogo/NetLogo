// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ Syntax, SyntaxJ }
import org.nlogo.api.{ Color, LogoList }
import org.nlogo.nvm.{ Context, Reporter }

class _basecolors extends Reporter {

  override def syntax =
    SyntaxJ.reporterSyntax(Syntax.ListType)

  override def report(context: Context): LogoList =
    report_1(context)
  def report_1(context: Context): LogoList =
    Color.BaseColors
}
