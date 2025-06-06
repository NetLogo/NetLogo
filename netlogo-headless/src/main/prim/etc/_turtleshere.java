// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Patch;
import org.nlogo.agent.Turtle;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final class _turtleshere
    extends Reporter {

  @Override
  public Object report(final Context context) {
    return report_1(context);
  }

  public AgentSet report_1(Context context) {
    Patch patch;
    if (context.agent instanceof Turtle) {
      patch = ((Turtle) context.agent).getPatchHere();
    } else {
      patch = (Patch) context.agent;
    }
    return patch.turtlesHereAgentSet();
  }
}
