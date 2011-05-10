package org.nlogo.prim.threed;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _turtlesat
    extends Reporter {
  @Override
  public Syntax syntax() {
    int[] right = {Syntax.TYPE_NUMBER,
        Syntax.TYPE_NUMBER,
        Syntax.TYPE_NUMBER};
    int ret = Syntax.TYPE_TURTLESET;
    return Syntax.reporterSyntax(right, ret, "-TP-");
  }

  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    double dx = argEvalDoubleValue(context, 0);
    double dy = argEvalDoubleValue(context, 1);
    double dz = argEvalDoubleValue(context, 2);
    org.nlogo.agent.Patch patch = null;
    try {
      patch = ((org.nlogo.agent.Agent3D) (context.agent)).getPatchAtOffsets(dx, dy, dz);
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
            (org.nlogo.agent.Turtle.class, patch.turtleCount(), false, world);
    for (org.nlogo.agent.Turtle turtle : patch.turtlesHere()) {
      if (turtle != null) {
        agentset.add(turtle);
      }
    }
    return agentset;
  }
}
