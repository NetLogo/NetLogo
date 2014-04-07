// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Syntax
import org.nlogo.agent.{ Patch, Turtle }
import org.nlogo.nvm.{ Context, Reporter }

class _patchhere extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.PatchType,
      agentClassString = "-T--")
  override def report(context: Context): Patch =
    report_1(context)
  def report_1(context: Context): Patch =
    context.agent.asInstanceOf[Turtle].getPatchHere
}
