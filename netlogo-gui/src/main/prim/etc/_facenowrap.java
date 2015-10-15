// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;

public final strictfp class _facenowrap
    extends Command {
  // turtle only since face for the observer is always nowrap -- AZS 4/12/05
  @Override
  public org.nlogo.core.Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.AgentType()}, "-T--", true);
  }

  @Override
  public void perform(final Context context)
      throws LogoException {
    org.nlogo.agent.Agent agentToFace = argEvalAgent(context, 0);
    if (agentToFace.id == -1) {
      throw new EngineException(context, this,
        I18N.errorsJ().getN("org.nlogo.$common.thatAgentIsDead", agentToFace.classDisplayName()));
    }
    org.nlogo.agent.Turtle turtle = (org.nlogo.agent.Turtle) context.agent;
    turtle.face(agentToFace, false);
    context.ip = next;
  }
}
