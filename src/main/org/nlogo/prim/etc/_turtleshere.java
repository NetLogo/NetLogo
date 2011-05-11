package org.nlogo.prim.etc;

import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.ArrayAgentSet;
import org.nlogo.agent.Patch;
import org.nlogo.agent.Turtle;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _turtleshere
    extends Reporter {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (Syntax.TYPE_TURTLESET, "-TP-");
  }

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
