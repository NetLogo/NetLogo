// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ Turtle, Patch }
import org.nlogo.api.{ Syntax, Nobody }
import org.nlogo.nvm.{ Reporter, Context }

class _patcheast extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(
      Syntax.PatchType, "-TP-")

  override def report(context: Context) =
    report_1(context)

  def report_1(context: Context) = {
    val result = world.topology.getPE(
      context.agent match {
        case p: Patch => p
        case t: Turtle => t.getPatchHere
      })
    if (result == null) Nobody
    else result
  }

}
