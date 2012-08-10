// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Link;
import org.nlogo.agent.Turtle;
import org.nlogo.api.AgentKindJ;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;

public final strictfp class _layoutradial
    extends Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.TurtlesetType(), Syntax.LinksetType(),
            Syntax.TurtleType()},
            true);
  }

  @Override
  public void perform(final Context context)
      throws LogoException {
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
