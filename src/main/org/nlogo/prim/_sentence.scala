// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.{ Syntax, LogoList, LogoListBuilder }
import org.nlogo.nvm.{ Context, Reporter, Pure, CustomGenerated }

class _sentence extends Reporter with Pure with CustomGenerated {

  override def syntax =
    Syntax.reporterSyntax(
      Array(Syntax.RepeatableType | Syntax.WildcardType),
      Syntax.ListType, dfault = 2, minimum = 0)

  override def report(context: Context): LogoList = {
    val builder = new LogoListBuilder
    var i = 0
    while(i < args.length) {
      args(i).report(context) match {
        case list: LogoList =>
          builder.addAll(list)
        case x =>
          builder.add(x)
      }
      i += 1
    }
    builder.toLogoList
  }

}
