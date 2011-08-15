package org.nlogo.prim.threed

import org.nlogo.agent.Protractor3D
import org.nlogo.api.LogoException
import org.nlogo.nvm.{ Context, Reporter, Syntax }

class _distancexyznowrap extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      Array(Syntax.TYPE_NUMBER, Syntax.TYPE_NUMBER, Syntax.TYPE_NUMBER),
      Syntax.TYPE_NUMBER, "-TP-")
  override def report(context: Context) =
    newValidDouble(
      world.protractor.asInstanceOf[Protractor3D].distance(
        context.agent,
        argEvalDoubleValue(context, 0),
        argEvalDoubleValue(context, 1),
        argEvalDoubleValue(context, 2),
        false))  // wrap = false
}
