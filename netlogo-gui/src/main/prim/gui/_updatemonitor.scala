// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.gui

import org.nlogo.nvm.{ Command, Context }
import org.nlogo.window.MonitorWidget

class _updatemonitor extends Command {
  switches = true



  override def perform(context: Context): Unit = {
    context.job.owner.asInstanceOf[MonitorWidget]
      .value(args(0).report(context))
    context.ip = next
  }
}
