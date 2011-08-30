package org.nlogo.prim.threed

import org.nlogo.agent.World3D
import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Reporter }

class _randomzcor extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.NumberType)
  override def report(context: Context) = {
    val w = world.asInstanceOf[World3D]
    val min = w.minPzcor - 0.5
    val max = w.maxPzcor + 0.5
    java.lang.Double.valueOf(
      min + context.job.random.nextDouble() * (max - min))
  }
}
