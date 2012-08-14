// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.threed

import org.nlogo.api.{ AgentException, Syntax }
import org.nlogo.nvm.{ Context, EngineException, Reporter }

class _towardspitchxyznowrap extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Array(Syntax.NumberType,
                                Syntax.NumberType,
                                Syntax.NumberType),
                          Syntax.NumberType, "-TP-")
  override def report(context: Context) =
    try newValidDouble(world.protractor.towardsPitch(
      context.agent,
      argEvalDoubleValue(context, 0),
      argEvalDoubleValue(context, 1),
      argEvalDoubleValue(context, 2),
      false)) // true = don't wrap
    catch {
      case ex: AgentException =>
        throw new EngineException(context, this, ex.getMessage)
    }
}
