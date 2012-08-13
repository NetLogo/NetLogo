// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ Turtle, Patch }
import org.nlogo.api.{ Syntax, Nobody }
import org.nlogo.nvm.{ Reporter, Context }

class _patchwest extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(
      Syntax.PatchType, "-TP-")

  override def report(context: Context) =
    report_1(context)

  def report_1(context: Context) = {
    val result =
      (context.agent match {
         case p: Patch => p
         case t: Turtle => t.getPatchHere
      }).getPatchWest
    if (result == null) Nobody
    else result
  }

}
