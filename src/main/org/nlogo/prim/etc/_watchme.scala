// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ Perspective, Syntax }
import org.nlogo.nvm.{ Command, Context }

class _watchme extends Command {
  override def syntax =
    Syntax.commandSyntax("-TPL", true)
  override def perform(context: Context) {
    world.observer.setPerspective(Perspective.Watch, context.agent)
    context.ip = next
  }
}
