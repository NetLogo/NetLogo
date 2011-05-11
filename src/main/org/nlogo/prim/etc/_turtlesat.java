package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _turtlesat
    extends Reporter {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (new int[]{Syntax.TYPE_NUMBER, Syntax.TYPE_NUMBER},
            Syntax.TYPE_TURTLESET, "-TP-");
  }

  @Override
  public Object report(final Context context)
      throws LogoException {
    double dx = argEvalDoubleValue(context, 0);
    double dy = argEvalDoubleValue(context, 1);
    org.nlogo.agent.Patch patch = null;
    try {
      patch = context.agent.getPatchAtOffsets(dx, dy);
    } catch (org.nlogo.api.AgentException e) {
      return new org.nlogo.agent.ArrayAgentSet(org.nlogo.agent.Turtle.class, 0,
          false, world);
    }
    if (patch == null) {
      return new org.nlogo.agent.ArrayAgentSet(org.nlogo.agent.Turtle.class, 0,
          false, world);
    }
    org.nlogo.agent.AgentSet agentset =
        new org.nlogo.agent.ArrayAgentSet
            (org.nlogo.agent.Turtle.class, patch.turtleCount(),
                false, world);
    for (org.nlogo.agent.Turtle turtle : patch.turtlesHere()) {
      if (turtle != null) {
        agentset.add(turtle);
      }
    }
    return agentset;
  }
}
