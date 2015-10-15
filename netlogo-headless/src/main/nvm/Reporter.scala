// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.agent.{ Agent, AgentSet }

abstract class Reporter extends Instruction {

  def report(context: Context): AnyRef

  final def checkAgentClass(agent: Agent, context: Context) {
    if ((agent.agentBit & agentBits) == 0)
      throwAgentClassException(context, agent.kind)
  }

  final def checkAgentSetClass(agents: AgentSet, context: Context) {
    if ((agents.agentBit & agentBits) == 0)
      throwAgentClassException(context, agents.kind)
  }

}
