// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.threed

import org.nlogo.api.{ AgentException, I18N, Syntax }
import org.nlogo.nvm.{ Context, EngineException, Reporter }

class _towardspitchnowrap extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Array(Syntax.TurtleType | Syntax.PatchType),
      Syntax.NumberType, "-TP-")
  override def report(context: Context) = {
    val agent = argEvalAgent(context, 0)
    if (agent.id == -1)
      throw new EngineException(
        context, this, I18N.errors.getN("org.nlogo.$common.thatAgentIsDead",
          agent.classDisplayName))
    try newValidDouble(world.protractor.towardsPitch(
      context.agent, agent, false)) // false = nowrap
    catch {
      case ex: AgentException =>
        throw new EngineException(context, this, ex.getMessage)
    }
  }
}
