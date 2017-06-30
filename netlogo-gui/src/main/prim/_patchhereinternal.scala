// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

// needed by _patchat.optimize() because regular _patchhere is turtle-only
import org.nlogo.agent.{ Patch, Turtle }
import org.nlogo.nvm.{ Context, Reporter }

class _patchhereinternal extends Reporter {


  override def report(context: Context) = report_1(context)

  def report_1(context: Context): Patch = context.agent match {
    case patch: Patch => patch
    case turtle: Turtle => turtle.getPatchHere
    case _ => world.fastGetPatchAt(0, 0)
  }
}
