// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.{ Patch, Turtle }
import org.nlogo.core.{ Syntax, SyntaxJ }
import org.nlogo.nvm.{ Context, Reporter }

class _patchhere extends Reporter {
  override def syntax =
    SyntaxJ.reporterSyntax(Syntax.PatchType, "-T--")
  override def report(context: Context): Patch =
    report_1(context)
  def report_1(context: Context): Patch =
    context.agent.asInstanceOf[Turtle].getPatchHere
}
