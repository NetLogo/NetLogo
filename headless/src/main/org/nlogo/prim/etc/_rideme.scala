// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.nvm.{ Command, Context }
import org.nlogo.api.{ Perspective, Syntax }

class _rideme extends Command {
  override def syntax =
    Syntax.commandSyntax("-T--", true)
  override def perform(context: Context) {
    world.observer.setPerspective(Perspective.Ride, context.agent)
    context.ip = next
  }
}
