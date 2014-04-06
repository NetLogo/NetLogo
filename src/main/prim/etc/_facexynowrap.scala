// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Syntax
import org.nlogo.agent.Turtle
import org.nlogo.nvm.{ Command, Context }

class _facexynowrap extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.NumberType, Syntax.NumberType),
      agentClassString = "-T--",
      switches = true)
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
