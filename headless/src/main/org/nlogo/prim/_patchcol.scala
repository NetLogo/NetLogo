// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.{ Syntax, AgentKind }
import org.nlogo.agent.{ Patch, ArrayAgentSet }
import org.nlogo.nvm.{ Reporter, Context }

class _patchcol extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(Array(Syntax.NumberType),
                          Syntax.PatchsetType)

  override def report(context: Context): AnyRef = {
    val result = new ArrayAgentSet(
      AgentKind.Patch, world.worldHeight, false, world)
    val xDouble = argEvalDoubleValue(context, 0)
    val x = xDouble.toInt
    if (x == xDouble && x >= world.minPxcor && x <= world.maxPxcor) {
      val yMax = world.maxPycor
      var y = world.minPycor
      while(y <= yMax) {
        result.add(world.fastGetPatchAt(x, y))
        y += 1
      }
    }
    result
  }

}
