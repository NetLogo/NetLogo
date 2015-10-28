// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.AgentKind
import org.nlogo.agent.{ AgentSet, ArrayAgentSet }
import org.nlogo.api.{ LogoException, Syntax }
import org.nlogo.nvm.{ Context, Reporter }

class _patchcol extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      Array[Int](Syntax.NumberType),
      Syntax.PatchsetType)

  override def report(context: Context): AnyRef = {
    val result = new ArrayAgentSet(AgentKind.Patch, world.worldHeight, false)
    val xDouble = argEvalDoubleValue(context, 0)
    val x = xDouble.toInt
    if (x == xDouble && x >= world.minPxcor && x <= world.maxPxcor) {
      var y = world.minPycor
      while (y <= world.maxPycor) {
        result.add(world.fastGetPatchAt(x, y))
        y+=1
      }
    }
    result
  }
}
