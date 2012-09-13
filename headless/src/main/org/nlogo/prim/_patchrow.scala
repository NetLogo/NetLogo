// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.{ Syntax, AgentKind }
import org.nlogo.agent.{ Patch, ArrayAgentSet }
import org.nlogo.nvm.{ Reporter, Context }

class _patchrow extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(Array(Syntax.NumberType),
                          Syntax.PatchsetType)

  override def report(context: Context): AnyRef = {
    val result = new ArrayAgentSet(
      AgentKind.Patch, world.worldHeight, false, world)
    val yDouble = argEvalDoubleValue(context, 0)
    val y = yDouble.toInt
    if (y == yDouble && y >= world.minPycor && y <= world.maxPycor) {
      val xMax = world.maxPxcor
      var x = world.minPxcor
      while(x <= xMax) {
        result.add(world.fastGetPatchAt(x, y))
        x += 1
      }
    }
    result
  }

}
