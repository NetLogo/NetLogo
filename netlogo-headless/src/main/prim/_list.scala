// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.LogoListBuilder
import org.nlogo.core.{ LogoList, Pure, Syntax }
import org.nlogo.nvm.{ Context, CustomGenerated, Reporter }

class _list extends Reporter with Pure with CustomGenerated {
  override def returnType =
    Syntax.ListType
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
