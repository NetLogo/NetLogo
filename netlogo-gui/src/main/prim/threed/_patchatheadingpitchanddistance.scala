// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.threed

import org.nlogo.agent.{ Protractor3D, Patch3D, Turtle3D }
import org.nlogo.api.{ AgentException, Nobody, Syntax }
import org.nlogo.nvm.{ Context, Reporter }

class _patchatheadingpitchanddistance extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      Array(Syntax.NumberType,
            Syntax.NumberType,
            Syntax.NumberType),
      Syntax.PatchType, "-TP-")
  override def report(context: Context) =
    try context.agent match {
      case turtle: Turtle3D =>
        world.protractor.asInstanceOf[Protractor3D].getPatchAtHeadingPitchAndDistance(
          turtle.xcor, turtle.ycor, turtle.zcor,
          argEvalDoubleValue(context, 0),
          argEvalDoubleValue(context, 1),
          argEvalDoubleValue(context, 2))
      case patch: Patch3D =>
        world.protractor.asInstanceOf[Protractor3D].getPatchAtHeadingPitchAndDistance(
          patch.pxcor, patch.pycor, patch.pzcor,
          argEvalDoubleValue(context, 0),
          argEvalDoubleValue(context, 1),
          argEvalDoubleValue(context, 2))
    }
    catch { case _: AgentException => Nobody }
}
