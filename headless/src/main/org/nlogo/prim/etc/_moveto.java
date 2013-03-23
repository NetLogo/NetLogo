// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.Agent;
import org.nlogo.agent.Turtle;
import org.nlogo.api.AgentException;
import org.nlogo.api.I18N;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.EngineException;

public final strictfp class _moveto
    extends Command {
  @Override
  public Syntax syntax() {
    int[] right = {Syntax.TurtleType() | Syntax.PatchType()};
    return Syntax.commandSyntax(right, "-T--", true);
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context) {
    Agent otherAgent = argEvalAgent(context, 0);
    if (otherAgent.id() == -1) {
      throw new EngineException(context, this,
          I18N.errorsJ().getN("org.nlogo.$common.thatAgentIsDead", otherAgent.classDisplayName()));
    }
    if (otherAgent instanceof org.nlogo.agent.Link) {
      throw new EngineException(context, this, "you can't move-to a link");
    }
    try {
      ((Turtle) context.agent).moveTo(otherAgent);
    } catch (AgentException ex) {
      throw new EngineException(context, this, ex.getMessage());
    }


    context.ip = next;
  }
}
