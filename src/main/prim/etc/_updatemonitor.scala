// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Syntax
import org.nlogo.nvm.{ Command, Context }

class _updatemonitor extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.WildcardType),
      agentClassString = "O---",
      switches = true)
  override def perform(context: Context) {
    workspace.updateMonitor(
      context.job.owner,
      args(0).report(context))
    context.ip = next
  }
}
