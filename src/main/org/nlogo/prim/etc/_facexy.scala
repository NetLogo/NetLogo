// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.Turtle
import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Command, Context }

class _facexy extends Command {
  override def syntax =
    Syntax.commandSyntax(
      Array(Syntax.NumberType, Syntax.NumberType),
      "-T--", true)
  override def perform(context: Context) {
    context.agent.asInstanceOf[Turtle].face(
      argEvalDoubleValue(context, 0),
      argEvalDoubleValue(context, 1),
      true)
    context.ip = next
  }
}
