package org.nlogo.prim.threed

import org.nlogo.agent.World3D
import org.nlogo.api.{ AgentException, LogoException, Nobody }
import org.nlogo.nvm.{ Context, Reporter, Syntax }

class _patch extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      Array(Syntax.TYPE_NUMBER, Syntax.TYPE_NUMBER, Syntax.TYPE_NUMBER),
      Syntax.TYPE_PATCH | Syntax.TYPE_NOBODY)
  override def report(context: Context) =
    try world.asInstanceOf[World3D].getPatchAt(
      argEvalDoubleValue(context, 0),
      argEvalDoubleValue(context, 1),
      argEvalDoubleValue(context, 2))
    catch { case _: AgentException => Nobody }
}
