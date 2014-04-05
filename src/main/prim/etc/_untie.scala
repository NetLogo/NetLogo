// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.Link
import org.nlogo.core.{ Syntax, SyntaxJ }
import org.nlogo.nvm.{ Command, Context }

class _untie extends Command {
  override def syntax =
    SyntaxJ.commandSyntax("---L", true)
  override def perform(context: Context) {
    context.agent.asInstanceOf[Link].mode(Link.MODE_NONE)
    context.ip = next
  }
}
