package org.nlogo.prim.threed

import org.nlogo.api.AgentException
import org.nlogo.nvm.{ Context, EngineException, Reporter, Syntax }

class _towardspitchxyznowrap extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Array(Syntax.TYPE_NUMBER,
                                Syntax.TYPE_NUMBER,
                                Syntax.TYPE_NUMBER),
                          Syntax.TYPE_NUMBER, "-TP-")
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
