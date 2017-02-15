// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.hubnet;

import org.nlogo.agent.Agent;
import org.nlogo.api.LogoException;
import org.nlogo.api.PerspectiveJ;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;

public strictfp class _hubnetsendfollow
    extends HubNetCommand {


  @Override
  public void perform(final Context context) throws LogoException {
    final String client = argEvalString(context, 0);
    final Agent agent = argEvalAgent(context, 1);
    final double radius = argEvalDoubleValue(context, 2);

    workspace.waitFor
        (new org.nlogo.api.CommandRunnable() {
          public void run() {
            hubNetManager().get().sendAgentPerspective
                (client,
                    PerspectiveJ.FOLLOW, agent.kind(), agent.id, radius, false);
          }
        });
    context.ip = next;
  }
}
