// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Syntax
import org.nlogo.api.Perspective
import org.nlogo.nvm.{ Command, Context }

class _watchme extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "-TPL",
      switches = true)
  override def perform(context: Context) {
    world.observer.setPerspective(Perspective.Watch, context.agent)
    context.ip = next
  }
}
