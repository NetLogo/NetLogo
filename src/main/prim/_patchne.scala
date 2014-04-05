// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.{ Syntax, SyntaxJ }
import org.nlogo.api.Nobody
import org.nlogo.agent.{ Turtle, Patch }
import org.nlogo.nvm.{ Reporter, Context }

class _patchne extends Reporter {

  override def syntax =
    SyntaxJ.reporterSyntax(
      Syntax.PatchType, "-TP-")

  override def report(context: Context): AnyRef =
    report_1(context)

  def report_1(context: Context): AnyRef = {
    val result = world.topology.getPNE(
      context.agent match {
        case p: Patch => p
        case t: Turtle => t.getPatchHere
      })
    if (result == null) Nobody
    else result
  }

}
