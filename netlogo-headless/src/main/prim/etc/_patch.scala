// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.AgentException
import org.nlogo.core.Nobody
import org.nlogo.nvm.{ Context, Reporter }

class _patch extends Reporter {

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
