// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.threed

import org.nlogo.api.AgentException
import org.nlogo.core.I18N
import org.nlogo.nvm.{ Context, Reporter, RuntimePrimitiveException }

class _towardspitch extends Reporter {

  override def report(context: Context) = {
    val agent = argEvalAgent(context, 0)
    if (agent.id == -1)
      throw new RuntimePrimitiveException(
        context, this, I18N.errors.getN("org.nlogo.$common.thatAgentIsDead",
          agent.classDisplayName))
    try newValidDouble(world.protractor.towardsPitch(context.agent, agent, true), context) // true = wrap
    catch {
      case ex: AgentException =>
        throw new RuntimePrimitiveException(context, this, ex.getMessage)
    }
  }
}
