package org.nlogo.prim.etc;

import org.nlogo.agent.AgentSet;
import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.ArgumentTypeException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;

public final strictfp class _averagepathlength extends Reporter {
  @Override
  public Syntax syntax() {
    int[] right = {Syntax.TYPE_TURTLESET, Syntax.TYPE_LINKSET};
    int ret = Syntax.TYPE_NUMBER;
    return Syntax.reporterSyntax(right, ret);
  }

  @Override
  public Object report(Context context) throws LogoException {
    return report_1(context, argEvalAgentSet(context, 0),
        argEvalAgentSet(context, 1));
  }

  public double report_1(Context context, AgentSet nodeSet, AgentSet linkBreed)
      throws LogoException {
    if (nodeSet.type() != org.nlogo.agent.Turtle.class) {
      throw new ArgumentTypeException
          (context, this, 0, Syntax.TYPE_TURTLESET, nodeSet);
    }
    if (linkBreed != world.links() && !world.isLinkBreed(linkBreed)) {
      throw new EngineException (context, this,
              I18N.errorsJ().get("org.nlogo.prim.etc.$common.expectedLastInputToBeLinkBreed"));
    }
    return world.linkManager.networkMetrics
        .averagePathLength(nodeSet, linkBreed);
  }
}
