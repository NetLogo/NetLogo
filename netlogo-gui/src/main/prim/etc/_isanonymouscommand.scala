// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Syntax
import org.nlogo.nvm.{ CommandTask, Context, Pure, Reporter }

class _isanonymouscommand extends Reporter with Pure {
  override def report(context: Context) =
    Boolean.box(
      args(0).report(context).isInstanceOf[CommandTask])
}
