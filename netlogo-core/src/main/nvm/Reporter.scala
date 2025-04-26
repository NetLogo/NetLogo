// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api.LogoException
import org.nlogo.agent.{ Agent, AgentSet }

abstract class Reporter extends Instruction {

  @throws(classOf[LogoException])
  def report(context: Context): AnyRef

  final def checkAgentClass(agent: Agent, context: Context): Unit = {
    if ((agent.agentBit & agentBits) == 0)
      throwAgentClassException(context, agent.kind)
  }

  final def checkAgentSetClass(agents: AgentSet, context: Context): Unit = {
    if ((agents.agentBit & agentBits) == 0)
      throwAgentClassException(context, agents.kind)
  }

}
