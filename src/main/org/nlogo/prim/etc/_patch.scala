// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ Syntax, AgentException, Nobody }
import org.nlogo.nvm.{ Reporter, Context }

class _patch extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(
      Array(Syntax.NumberType, Syntax.NumberType),
      Syntax.PatchType | Syntax.NobodyType)

  // I've tried to rejigger this and the result gets past TryCatchSafeChecker but then doesn't work
  // at runtime ("Inconsistent stack height") - ST 2/10/09
  override def report(context: Context): AnyRef =
    try world.getPatchAt(
      argEvalDoubleValue(context, 0),
      argEvalDoubleValue(context, 1))
    catch {
      case _: AgentException => Nobody
    }

}
