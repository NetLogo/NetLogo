package org.nlogo.prim.etc;

import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Turtle;
import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _networkshortestpathnodes extends Reporter {
  @Override
  public Syntax syntax() {
    int[] right = {Syntax.TYPE_TURTLE, Syntax.TYPE_LINKSET};
    int ret = Syntax.TYPE_LIST;
    return Syntax.reporterSyntax(right, ret, "-T--");
  }

  @Override
  public Object report(final Context context)
      throws LogoException {
    return report_1(context,
        argEvalTurtle(context, 0),
        argEvalAgentSet(context, 1));
  }

  public LogoList report_1(final Context context, Turtle destNode, AgentSet linkBreed)
      throws LogoException {
    if (linkBreed != world.links() && !world.isLinkBreed(linkBreed)) {
      throw new EngineException (context, this,
              I18N.errorsJ().get("org.nlogo.prim.etc.$common.expectedLastInputToBeLinkBreed"));
    }

    if (destNode.id == -1) {
      throw new EngineException(context, this,
        I18N.errorsJ().getN("org.nlogo.$common.thatAgentIsDead", destNode.classDisplayName()));
    }
    return world.linkManager.networkMetrics.networkShortestPathNodes
        (context.job.random, (Turtle) context.agent, destNode, linkBreed);
  }
}
