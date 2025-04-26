// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.threed

import org.nlogo.agent.{ Patch3D, Protractor3D, Turtle3D }
import org.nlogo.api.AgentException
import org.nlogo.core.Nobody
import org.nlogo.nvm.{ Context, Reporter }

class _patchatheadingpitchanddistance extends Reporter {

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
      case a =>
        throw new IllegalStateException
    }
    catch { case _: AgentException => Nobody }
}
