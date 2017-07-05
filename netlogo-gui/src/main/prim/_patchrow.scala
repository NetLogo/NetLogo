// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.AgentSetBuilder
import org.nlogo.core.AgentKind
import org.nlogo.nvm.{ Context, Reporter }

class _patchrow extends Reporter {


  override def report(context: Context): AnyRef = {
    val result = new AgentSetBuilder(AgentKind.Patch, world.worldWidth)
    val yDouble = argEvalDoubleValue(context, 0)
    val y = yDouble.toInt
    if (y == yDouble && y >= world.minPycor && y <= world.maxPycor) {
      var x = world.minPxcor
      while (x <= world.maxPxcor) {
        result.add(world.fastGetPatchAt(x, y))
        x+=1
      }
    }
    result.build()
  }
}
