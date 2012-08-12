// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ I18N, Syntax }
import org.nlogo.nvm.{ Command, Context, EngineException }

class _inspect extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.AgentType))
  override def perform(context: Context) {
    val agent = argEvalAgent(context, 0)
    if (agent.id == -1)
      throw new EngineException(context, this,
        I18N.errors.getN("org.nlogo.$common.thatAgentIsDead", agent.classDisplayName))
    // we usually use a default radius of 3, but that doesnt work when the world has a radius
    // of less than 3. so simply take the miniumum. - JC 7/1/10
    val minWidthOrHeight =
      (workspace.world.worldWidth  / 2) min
      (workspace.world.worldHeight / 2)
    val radius = 3 min (minWidthOrHeight / 2)
    workspace.inspectAgent(agent.kind, agent, radius)
    context.ip = next
  }
}
