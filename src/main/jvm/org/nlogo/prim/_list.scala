// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.{ LogoException, LogoListBuilder, Syntax }
import org.nlogo.nvm.{ Reporter, Context, Pure, CustomGenerated }

class _list extends Reporter with Pure with CustomGenerated {
  override def syntax =
    Syntax.reporterSyntax(
      Array(Syntax.RepeatableType | Syntax.WildcardType),
      Syntax.ListType, 2, 0)
  override def report(context: Context) = {
    val builder = new LogoListBuilder
    var i = 0
    while(i < args.length) {
      builder.add(args(i).report(context))
      i += 1
    }
    builder.toLogoList
  }
}
