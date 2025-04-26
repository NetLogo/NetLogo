// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.Dump
import org.nlogo.nvm.{ Command, Context, RuntimePrimitiveException }

class _randomseed extends Command {

  override def perform(context: Context): Unit = {
    perform_1(context, argEvalDoubleValue(context, 0))
  }

  def perform_1(context: Context, arg0: Double): Unit = {
    val l = arg0.toLong
    if (l < -2147483648 || l > 2147483647)
      throw new RuntimePrimitiveException(
        context, this,
        Dump.number(arg0) + " is not in the allowable range for random seeds (-2147483648 to 2147483647)")
    val seed = l.toInt
    context.job.random.setSeed(seed)
    if (context.job.workspace != null && context.job.random == context.job.workspace.world.mainRNG) {
      context.job.workspace.plotRNG.setSeed(seed)
    }
    context.ip = next
  }

}
