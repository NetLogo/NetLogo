// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.threed

import org.nlogo.agent.Agent3D
import org.nlogo.api.{ AgentException, Nobody, Syntax }
import org.nlogo.nvm.{ Context, Reporter }

class _patchat extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Array(Syntax.NumberType,
                                Syntax.NumberType,
                                Syntax.NumberType),
                          Syntax.PatchType, "-TP-")
  override def report(context: Context) = {
    try context.agent.asInstanceOf[Agent3D].getPatchAtOffsets(
      argEvalDoubleValue(context, 0),
      argEvalDoubleValue(context, 1),
      argEvalDoubleValue(context, 2))
    catch { case _: AgentException => Nobody }
  }
}
