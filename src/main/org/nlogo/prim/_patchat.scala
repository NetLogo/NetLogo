// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.Patch
import org.nlogo.api.{ Syntax, AgentException, Nobody }
import org.nlogo.nvm.{ Reporter, Context }

class _patchat extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(
      Array(Syntax.NumberType, Syntax.NumberType),
      Syntax.PatchType | Syntax.NobodyType,
      "-TP-")

  // I've tried to rejigger this and the result gets past TryCatchSafeChecker but then
  // doesn't work at runtime ("Inconsistent stack height") - ST 2/10/09
  override def report(context: Context): AnyRef = {
    val dx = argEvalDoubleValue(context, 0)
    val dy = argEvalDoubleValue(context, 1)
    try context.agent.getPatchAtOffsets(dx, dy)
    catch { case e: AgentException => Nobody }
  }

}
