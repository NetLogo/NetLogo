// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.threed

import org.nlogo.api.AgentException
import org.nlogo.nvm.{ Context, Reporter, RuntimePrimitiveException }

class _towardspitchxyz extends Reporter {

  override def report(context: Context) =
    try newValidDouble(
      world.protractor.towardsPitch(
        context.agent,
        argEvalDoubleValue(context, 0),
        argEvalDoubleValue(context, 1),
        argEvalDoubleValue(context, 2),
        true), context) // true = wrap
    catch {
      case ex: AgentException =>
        throw new RuntimePrimitiveException(context, this, ex.getMessage)
    }
}
