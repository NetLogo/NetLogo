// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.agent.{ Agent, AgentSet }
import org.nlogo.api.LogoException

abstract class Reporter extends Instruction {

  @throws(classOf[LogoException])
  def report(context: Context): AnyRef

  @throws(classOf[LogoException])
  final def checkAgentClass(agent: Agent, context: Context) {
    if ((agent.getAgentBit & agentBits) == 0)
      throwAgentClassException(context, agent.getAgentClass)
  }

  @throws(classOf[LogoException])
  final def checkAgentSetClass(agents: AgentSet, context: Context) {
    if ((agents.getAgentBit & agentBits) == 0)
      throwAgentClassException(context, agents.`type`)
  }

}
