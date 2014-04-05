// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.{ Syntax, SyntaxJ, AgentKind }
import org.nlogo.agent.{ Patch, AgentSetBuilder }
import org.nlogo.nvm.{ Reporter, Context }

class _patchcol extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType),
      ret = Syntax.PatchsetType)

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
