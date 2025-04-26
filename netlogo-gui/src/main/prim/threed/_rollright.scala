// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.threed

import org.nlogo.agent.Turtle3D
import org.nlogo.nvm.{ Command, Context }

class _rollright extends Command {


  switches = true
  override def perform(context: Context): Unit = {
    val delta = argEvalDoubleValue(context, 0)
    val t = context.agent.asInstanceOf[Turtle3D]
    t.roll(t.roll + delta)
    context.ip = next
  }
}
