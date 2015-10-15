// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Patch;
import org.nlogo.agent.Turtle;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final strictfp class _neighbors
    extends Reporter {
  @Override
  public org.nlogo.core.Syntax syntax() {
    return Syntax.reporterSyntax(Syntax.PatchsetType(), "-TP-");
  }

  @Override
  public Object report(Context context) {
    return report_1(context);
  }

  public AgentSet report_1(Context context) {
    if (context.agent instanceof Turtle) {
      return ((Turtle) context.agent).getPatchHere().getNeighbors();
    }
    return ((Patch) context.agent).getNeighbors();
  }
}
