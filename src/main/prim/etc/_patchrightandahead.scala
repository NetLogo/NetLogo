// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.Turtle
import org.nlogo.api.{ Syntax, AgentException, Nobody }
import org.nlogo.nvm.{ Context, Reporter }

@annotation.strictfp
class _patchrightandahead extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(
      Array(Syntax.NumberType, Syntax.NumberType),
      Syntax.PatchType, "-T--")

  override def report(context: Context): AnyRef =
    try
      context.agent.asInstanceOf[Turtle]
        .getPatchAtHeadingAndDistance(
          argEvalDoubleValue(context, 0),
          argEvalDoubleValue(context, 1))
    catch { case _: AgentException => Nobody }

}
