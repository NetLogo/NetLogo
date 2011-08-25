package org.nlogo.prim.threed

import org.nlogo.agent.Agent
import org.nlogo.api.{ I18N, AgentException }
import org.nlogo.nvm.{ Context, EngineException, Reporter, Syntax }

class _towardspitchnowrap extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Array(Syntax.TYPE_TURTLE | Syntax.TYPE_PATCH),
      Syntax.TYPE_NUMBER, "-TP-")
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
