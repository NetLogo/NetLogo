// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.threed

import org.nlogo.agent.Protractor3D
import org.nlogo.nvm.{ Context, Reporter }

class _distancexyznowrap extends Reporter {

  override def report(context: Context) =
    newValidDouble(
      world.protractor.asInstanceOf[Protractor3D].distance(
        context.agent,
        argEvalDoubleValue(context, 0),
        argEvalDoubleValue(context, 1),
        argEvalDoubleValue(context, 2),
        false), context)  // wrap = false
}
