// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ Turtle, Patch }
import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Reporter, Context }

// needed by _patchat.optimize() because regular _patchhere is turtle-only

class _patchhereinternal extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(
      Syntax.PatchType, "-TP-")

  override def report(context: Context): Patch =
    report_1(context)

  def report_1(context: Context): Patch =
    context.agent match {
      case p: Patch => p
      case t: Turtle => t.getPatchHere
    }

}
