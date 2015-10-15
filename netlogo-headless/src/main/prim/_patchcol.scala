// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.AgentSetBuilder
import org.nlogo.core.AgentKind
import org.nlogo.nvm.{ Context, Reporter }

class _patchcol extends Reporter {

  override def report(context: Context): AnyRef = {
    val builder = new AgentSetBuilder(AgentKind.Patch, world.worldHeight)
    val xDouble = argEvalDoubleValue(context, 0)
    val x = xDouble.toInt
    if (x == xDouble && x >= world.minPxcor && x <= world.maxPxcor) {
      val yMax = world.maxPycor
      var y = world.minPycor
      while(y <= yMax) {
        builder.add(world.fastGetPatchAt(x, y))
        y += 1
      }
    }
    builder.build()
  }

}
