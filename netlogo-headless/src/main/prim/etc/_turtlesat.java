// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.AgentSetBuilder;
import org.nlogo.core.AgentKindJ;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final class _turtlesat
    extends Reporter {

  @Override
  public Object report(final Context context) {
    double dx = argEvalDoubleValue(context, 0);
    double dy = argEvalDoubleValue(context, 1);
    org.nlogo.agent.Patch patch = null;
    try {
      patch = context.agent.getPatchAtOffsets(dx, dy);
    } catch (org.nlogo.api.AgentException e) {
      return world.noTurtles();
    }
    if (patch == null) {
      return world.noTurtles();
    }
    AgentSetBuilder builder = new AgentSetBuilder(
      AgentKindJ.Turtle(), patch.turtleCount());
    for (org.nlogo.agent.Turtle turtle : patch.turtlesHere()) {
      if (turtle != null) {
        builder.add(turtle);
      }
    }
    return builder.build();
  }
}
