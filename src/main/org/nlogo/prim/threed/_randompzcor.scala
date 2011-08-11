package org.nlogo.prim.threed

import org.nlogo.agent.World3D
import org.nlogo.nvm.{ Context, Reporter, Syntax }

class _randompzcor extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.TYPE_NUMBER)
  override def report(context: Context) = {
    val w = world.asInstanceOf[World3D]
    val min = w.minPzcor
    val max = w.maxPzcor
    java.lang.Double.valueOf(
      min + context.job.random.nextInt(max - min + 1))
  }
}
