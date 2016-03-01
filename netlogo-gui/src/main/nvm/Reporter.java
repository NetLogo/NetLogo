// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm;

import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentSet;
import org.nlogo.api.LogoException;

public abstract strictfp class Reporter
    extends Instruction {
  public abstract Object report(final Context context)
      throws LogoException;

  public final void checkAgentClass(Agent agent, Context context)
      throws LogoException {
    if ((agent.getAgentBit() & agentBits) == 0) {
      throwAgentClassException(context, agent.kind());
    }
  }

  public final void checkAgentSetClass(AgentSet agents, Context context)
      throws LogoException {
    if ((agents.getAgentBit() & agentBits) == 0) {
      throwAgentClassException(context, agents.kind());
    }
  }

}
