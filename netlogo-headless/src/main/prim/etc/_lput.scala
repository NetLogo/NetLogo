// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ LogoList, Pure }
import org.nlogo.nvm.{ Context, Reporter }

class _lput extends Reporter with Pure {
  override def report(context: Context): LogoList = {
    val obj = args(0).report(context)
    argEvalList(context, 1).lput(obj)
  }
}
