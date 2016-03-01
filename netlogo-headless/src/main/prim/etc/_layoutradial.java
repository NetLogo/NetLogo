// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Turtle;
import org.nlogo.core.AgentKindJ;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;

public final strictfp class _layoutradial
    extends Command {

  public _layoutradial() {
    switches = true;
  }

  @Override
  public void perform(final Context context) {
    AgentSet nodeset = argEvalAgentSet(context, 0, AgentKindJ.Turtle());
    AgentSet linkset = argEvalAgentSet(context, 1, AgentKindJ.Link());
    Turtle root = argEvalTurtle(context, 2);
    try {
      org.nlogo.agent.Layouts.radial(world, nodeset, linkset, root);
    } catch (org.nlogo.api.AgentException e) {
      throw new EngineException(context, this, e.getMessage());
    }
    context.ip = next;
  }
}
