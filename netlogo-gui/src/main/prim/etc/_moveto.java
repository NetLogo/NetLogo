// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.Agent;
import org.nlogo.agent.Turtle;
import org.nlogo.api.AgentException;
import org.nlogo.core.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.RuntimePrimitiveException;

public final strictfp class _moveto
    extends Command {
  public _moveto() {
    this.switches = true;
  }



  @Override
  public void perform(final org.nlogo.nvm.Context context)
      throws LogoException {
    Agent otherAgent = argEvalAgent(context, 0);
    if (otherAgent.id() == -1) {
      throw new RuntimePrimitiveException(context, this,
          I18N.errorsJ().getN("org.nlogo.$common.thatAgentIsDead", otherAgent.classDisplayName()));
    }
    if (otherAgent instanceof org.nlogo.agent.Link) {
      throw new RuntimePrimitiveException(context, this, "you can't move-to a link");
    }
    try {
      if (context.agent instanceof Turtle) {
        ((Turtle) context.agent).moveTo(otherAgent);
      } else {
        world.observer().moveto(otherAgent);
      }
    } catch (AgentException ex) {
      throw new RuntimePrimitiveException(context, this, ex.getMessage());
    }


    context.ip = next;
  }
}
