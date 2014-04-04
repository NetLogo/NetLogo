// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.nvm.{ CommandTask, Context, Pure, Reporter }

class _iscommandtask extends Reporter with Pure {
  override def report(context: Context): java.lang.Boolean =
    Boolean.box(
      args(0).report(context).isInstanceOf[CommandTask])
}
