package org.nlogo.prim.etc;

import java.util.Set;

import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.ArrayAgentSet;
import org.nlogo.agent.Turtle;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.ArgumentTypeException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _innetworkradius
    extends Reporter {
  @Override
  public Syntax syntax() {
    int left = Syntax.TYPE_TURTLESET;
    int[] right = {Syntax.TYPE_NUMBER, Syntax.TYPE_LINKSET};
    int ret = Syntax.TYPE_TURTLESET;
    return Syntax.reporterSyntax
        (left, right, ret, Syntax.NORMAL_PRECEDENCE + 2, false,
            "-T--", null);
  }

  @Override
  public Object report(final Context context) throws LogoException {
    return report_1(context, argEvalAgentSet(context, 0),
        argEvalDoubleValue(context, 1),
        argEvalAgentSet(context, 2));
  }

  public AgentSet report_1(final Context context, AgentSet sourceSet, double radius, AgentSet linkBreed)
      throws LogoException {
    if (sourceSet.type() != org.nlogo.agent.Turtle.class) {
      throw new ArgumentTypeException(context, this, 0, Syntax.TYPE_TURTLESET, sourceSet);
    }
    if (linkBreed != world.links() && !world.isLinkBreed(linkBreed)) {
      throw new EngineException
          (context, this, "expected last input to be a link breed.");
    }
    if (radius < 0) {
      throw new EngineException
          (context, this, displayName() + " should not be given a negative radius");
    }

    Set<Turtle> result =
        world.linkManager.networkMetrics.inNetworkRadius((Turtle) context.agent, sourceSet, radius, linkBreed);

    return new ArrayAgentSet
        (sourceSet.type(),
            result.toArray(new Agent[result.size()]),
            world);
  }
}
