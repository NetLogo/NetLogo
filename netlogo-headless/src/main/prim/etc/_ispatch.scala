// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.Patch
import org.nlogo.core.Pure
import org.nlogo.nvm.{ Context, Reporter }

class _ispatch extends Reporter with Pure {
  override def report(context: Context): java.lang.Boolean =
    Boolean.box(
      args(0).report(context).isInstanceOf[Patch])
}
