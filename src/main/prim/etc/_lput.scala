// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.LogoList
import org.nlogo.nvm.{ Context, Pure, Reporter }

class _lput extends Reporter with Pure {
  override def report(context: Context): LogoList = {
    val obj = args(0).report(context)
    argEvalList(context, 1).lput(obj)
  }
}
