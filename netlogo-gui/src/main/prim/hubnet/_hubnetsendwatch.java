// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.hubnet;

import org.nlogo.agent.Agent;
import org.nlogo.api.LogoException;
import org.nlogo.api.PerspectiveJ;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;

public strictfp class _hubnetsendwatch
    extends org.nlogo.nvm.Command {
  @Override
  public org.nlogo.core.Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]
            {Syntax.StringType(), Syntax.AgentType()},
            "OTPL");
  }

  @Override
  public void perform(final Context context) throws LogoException {
    final String client = argEvalString(context, 0);
    final Agent agent = argEvalAgent(context, 1);

    workspace.waitFor
        (new org.nlogo.api.CommandRunnable() {
          public void run() {
            workspace.getHubNetManager().sendAgentPerspective
                (client,
                    PerspectiveJ.WATCH().export(),
                    agent.getAgentClass(), agent.id, ((world.worldWidth() - 1) / 2), false);
          }
        });
    context.ip = next;
  }
}
