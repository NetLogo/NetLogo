// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ Patch, Turtle }
import org.nlogo.api.{ Nobody, Syntax }
import org.nlogo.nvm.{ Context, Reporter }

class _patchnw extends Reporter {
  override def syntax: Syntax =
    Syntax.reporterSyntax(Syntax.PatchType, "-TP-")

  override def report(context: Context) = report_1(context)

  def report_1(context: Context): AnyRef = {
    val patch = context.agent match {
      case patch: Patch => patch.getPatchNorthWest
      case turtle: Turtle => turtle.getPatchHere.getPatchNorthWest
      case _ => world.fastGetPatchAt(-1, 1)
    }
    if (patch != null) patch else Nobody
  }
}
