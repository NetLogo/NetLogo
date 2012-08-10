// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.{ Observer, Turtle }
import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Command, Context }

class _facexynowrap extends Command {
  override def syntax =
    Syntax.commandSyntax(
      Array(Syntax.NumberType, Syntax.NumberType),
      "-T--", true)
  override def perform(context: Context) {
    context.agent match {
      case turtle: Turtle =>
        turtle.face(
          argEvalDoubleValue(context, 0),
          argEvalDoubleValue(context, 1),
          false)
    }
    context.ip = next
  }
}
