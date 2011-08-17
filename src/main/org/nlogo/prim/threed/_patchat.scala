package org.nlogo.prim.threed

import org.nlogo.agent.{ Agent3D, Patch }
import org.nlogo.api.{ AgentException, Nobody }
import org.nlogo.nvm.{ Context, Reporter, Syntax }

class _patchat extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Array(Syntax.TYPE_NUMBER,
                                Syntax.TYPE_NUMBER,
                                Syntax.TYPE_NUMBER),
                          Syntax.TYPE_PATCH, "-TP-")
  override def report(context: Context) = {
    try context.agent.asInstanceOf[Agent3D].getPatchAtOffsets(
      argEvalDoubleValue(context, 0),
      argEvalDoubleValue(context, 1),
      argEvalDoubleValue(context, 2))
    catch { case _: AgentException => Nobody }
  }
}
