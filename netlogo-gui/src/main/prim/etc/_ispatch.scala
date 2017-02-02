// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.Patch
import org.nlogo.core.Syntax
import org.nlogo.nvm.{ Context, Reporter }
import org.nlogo.core.Pure

class _ispatch extends Reporter with Pure {

  override def report(context: Context) =
    Boolean.box(
      args(0).report(context).isInstanceOf[Patch])
}
