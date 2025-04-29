// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ Patch, Turtle }
import org.nlogo.core.Nobody
import org.nlogo.nvm.{ Context, Reporter }

class _patchne extends Reporter {

  override def report(context: Context): AnyRef =
    report_1(context)

  def report_1(context: Context): AnyRef = {
    val result = world.topology.getPNE(
      context.agent match {
        case p: Patch => p
        case t: Turtle => t.getPatchHere
        case a => throw new Exception(s"Unexpected agent: $a")
      })
    if (result == null) Nobody
    else result
  }

}
