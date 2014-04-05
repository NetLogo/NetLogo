// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.Syntax
import org.nlogo.api.{ LogoException, LogoList, LogoListBuilder }
import org.nlogo.nvm.{ Reporter, Context, Pure, CustomGenerated }

class _list extends Reporter with Pure with CustomGenerated {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.RepeatableType | Syntax.WildcardType),
      ret = Syntax.ListType,
      defaultOption = Some(2),
      minimumOption = Some(0))
  override def report(context: Context): LogoList = {
    val builder = new LogoListBuilder
    var i = 0
    while(i < args.length) {
      builder.add(args(i).report(context))
      i += 1
    }
    builder.toLogoList
  }
}
