// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.hubnet;

import org.nlogo.agent.Agent;
import org.nlogo.api.LogoException;
import org.nlogo.api.PerspectiveJ;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;

public strictfp class _hubnetsendwatch
    extends HubNetCommand {


  @Override
  public void perform(final Context context) throws LogoException {
    final String client = argEvalString(context, 0);
    final Agent agent = argEvalAgent(context, 1);

    workspace.waitFor
        (new org.nlogo.api.CommandRunnable() {
          public void run() {
            hubNetManager().get().sendAgentPerspective
                (client,
                    PerspectiveJ.WATCH,
                    agent.kind(), agent.id, ((world.worldWidth() - 1) / 2), false);
          }
        });
    context.ip = next;
  }
}
