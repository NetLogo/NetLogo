// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.threed

import org.nlogo.agent.Turtle3D
import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Command, Context }

class _rollright extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.NumberType), "-T--", true)
  override def perform(context: Context) {
    val delta = argEvalDoubleValue(context, 0)
    val t = context.agent.asInstanceOf[Turtle3D]
    t.roll(t.roll + delta)
    context.ip = next
  }
}
