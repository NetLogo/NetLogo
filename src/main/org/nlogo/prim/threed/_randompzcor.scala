// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.threed

import org.nlogo.agent.World3D
import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Reporter }

class _randompzcor extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.NumberType)
  override def report(context: Context) = {
    val w = world.asInstanceOf[World3D]
    val min = w.minPzcor
    val max = w.maxPzcor
    Double.box(
      min + context.job.random.nextInt(max - min + 1))
  }
}
