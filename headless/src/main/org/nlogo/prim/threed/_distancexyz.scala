// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.threed

import org.nlogo.agent.Protractor3D
import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Reporter }

class _distancexyz extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      Array(Syntax.NumberType, Syntax.NumberType, Syntax.NumberType),
      Syntax.NumberType, "-TP-")
  override def report(context: Context) =
    newValidDouble(
      world.protractor.asInstanceOf[Protractor3D].distance(
        context.agent,
        argEvalDoubleValue(context, 0),
        argEvalDoubleValue(context, 1),
        argEvalDoubleValue(context, 2),
        true)) // wrap = true
}
