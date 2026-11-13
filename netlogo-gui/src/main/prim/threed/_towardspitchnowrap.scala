// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.threed

import org.nlogo.api.AgentException
import org.nlogo.core.LocaleContext
import org.nlogo.nvm.{ Context, Reporter, RuntimePrimitiveException }

class _towardspitchnowrap extends Reporter {

  override def report(context: Context) = {
    val agent = argEvalAgent(context, 0)
    if (agent.id == -1)
      throw new RuntimePrimitiveException(context, this, loc"That ${agent.classDisplayName} is dead.")
    try newValidDouble(world.protractor.towardsPitch(
      context.agent, agent, false), context) // false = nowrap
    catch {
      case ex: AgentException =>
        throw new RuntimePrimitiveException(context, this, ex.getMessage)
    }
  }
}
