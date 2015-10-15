// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.Turtle
import org.nlogo.nvm.{ Command, Context }

class _facexy extends Command {
  switches = true
  override def perform(context: Context) {
    context.agent.asInstanceOf[Turtle].face(
      argEvalDoubleValue(context, 0),
      argEvalDoubleValue(context, 1),
      true)
    context.ip = next
  }
}
