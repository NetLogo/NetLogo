package org.nlogo.prim.gui

import org.nlogo.api.LogoException
import org.nlogo.nvm.{ Command, Context, Syntax }
import org.nlogo.window.MonitorWidget

class _updatemonitor extends Command {
  override def syntax =
    Syntax.commandSyntax(
      Array(Syntax.TYPE_WILDCARD), "O---", true);
  override def perform(context: Context) {
    context.job.owner.asInstanceOf[MonitorWidget]
      .value(args(0).report(context))
    context.ip = next
  }
}
