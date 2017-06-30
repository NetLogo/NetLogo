// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.threed

import org.nlogo.agent.Turtle3D
import org.nlogo.nvm.{ Context, Reporter }

class _dz extends Reporter {

  override def report(context: Context) = {
    val turtle = context.agent.asInstanceOf[Turtle3D]
    val value = turtle.dz
    validDouble(value, context)
    Double.box(
      if (StrictMath.abs(value) < 3.2e-15)
        0
      else
        value)
  }
}
