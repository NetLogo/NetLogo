package org.nlogo.prim.etc;

import org.nlogo.api.AgentException;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Link;
import org.nlogo.agent.Turtle;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;

public final strictfp class _layouttutte
    extends Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.TYPE_TURTLESET, Syntax.TYPE_LINKSET,
            Syntax.TYPE_NUMBER},
            true);
  }

  @Override
  public void perform(final Context context)
      throws LogoException {
    AgentSet nodeset = argEvalAgentSet(context, 0, Turtle.class);
    AgentSet linkset = argEvalAgentSet(context, 1, Link.class);
    double radius = argEvalDoubleValue(context, 2);
    try {
      org.nlogo.agent.Layouts.tutte(nodeset, linkset, radius,
          context.job.random);
    } catch (AgentException e) {
      throw new EngineException(context, this, e.getMessage());
    }
    context.ip = next;
  }
}
