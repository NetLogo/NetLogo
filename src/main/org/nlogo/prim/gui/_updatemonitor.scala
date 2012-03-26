// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.gui

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Command, Context }
import org.nlogo.window.MonitorWidget

class _updatemonitor extends Command {
  override def syntax =
    Syntax.commandSyntax(
      Array(Syntax.WildcardType), "O---", true);
  override def perform(context: Context) {
    context.job.owner.asInstanceOf[MonitorWidget]
      .value(args(0).report(context))
    context.ip = next
  }
}
