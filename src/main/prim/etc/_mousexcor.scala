// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.nvm.{ Context, Reporter }

class _mousexcor extends Reporter {
  override def report(context: Context): java.lang.Double =
    Double.box(workspace.mouseXCor)
}
