// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.AgentSetBuilder;
import org.nlogo.core.AgentKindJ;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final class _turtlesat
    extends Reporter {


  @Override
  public Object report(final Context context)
      throws LogoException {
    double dx = argEvalDoubleValue(context, 0);
    double dy = argEvalDoubleValue(context, 1);
    org.nlogo.agent.Patch patch = null;
    try {
      patch = context.agent.getPatchAtOffsets(dx, dy);
    } catch (org.nlogo.api.AgentException e) {
      return AgentSet.emptyTurtleSet();
    }
    if (patch == null) {
      return AgentSet.emptyTurtleSet();
    }
    AgentSetBuilder agentSetBuilder = new AgentSetBuilder(AgentKindJ.Turtle(), patch.turtleCount());
    for (org.nlogo.agent.Turtle turtle : patch.turtlesHere()) {
      if (turtle != null) {
        agentSetBuilder.add(turtle);
      }
    }
    return agentSetBuilder.build();
  }
}
