// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.threed

import org.nlogo.agent.{ Observer3D, Turtle3D }
import org.nlogo.nvm.{ Command, Context }

class _facexyz extends Command {
  switches = true



  override def perform(context: Context): Unit = {
    context.agent match {
      case turtle: Turtle3D =>
        turtle.face(argEvalDoubleValue(context, 0),
                    argEvalDoubleValue(context, 1),
                    argEvalDoubleValue(context, 2), true)
      case observer: Observer3D =>
        observer.face(argEvalDoubleValue(context, 0),
                      argEvalDoubleValue(context, 1),
                      argEvalDoubleValue(context, 2))
      case a =>
        throw new Exception(s"Unexpected agent: $a")
    }
    context.ip = next
  }
}
