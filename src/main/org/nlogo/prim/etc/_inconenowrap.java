// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Turtle;
import org.nlogo.api.AgentKindJ;
import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;

import java.util.List;

public final strictfp class _inconenowrap
    extends Reporter {
  @Override
  public Syntax syntax() {
    int left = Syntax.AgentsetType();
    int[] right = {Syntax.NumberType(), Syntax.NumberType()};
    int ret = Syntax.AgentsetType();
    return Syntax.reporterSyntax(left, right, ret, org.nlogo.api.Syntax.NormalPrecedence() + 2,
        false, "OTPL", "-T--");
  }

  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    return report_1(context, argEvalAgentSet(context, 0),
        argEvalDoubleValue(context, 1),
        argEvalDoubleValue(context, 2));
  }

  public AgentSet report_1(final org.nlogo.nvm.Context context, AgentSet sourceSet,
                           double radius, double angle)
      throws LogoException {
    if (sourceSet.kind() == AgentKindJ.Link()) {
      throw new EngineException
          (context, this, I18N.errorsJ().get("org.nlogo.prim.etc.$common.expectedTurtleOrPatchButGotLink"));
    }
    if (radius < 0) {
      throw new EngineException(context, this,
          I18N.errorsJ().getN("org.nlogo.prim.etc.$common.noNegativeRadius", displayName()));
    }
    if (angle < 0) {
      throw new EngineException(context, this,
          I18N.errorsJ().getN("org.nlogo.prim.etc.$common.noNegativeAngle", displayName()));
    }
    if (angle > 360) {
      throw new EngineException(context, this,
          I18N.errorsJ().getN("org.nlogo.prim.etc.$common.noAngleGreaterThan360", displayName()));
    }

    List<Agent> result =
        world.inRadiusOrCone.inCone((Turtle) context.agent, sourceSet, radius, angle, false);
    return new org.nlogo.agent.ArrayAgentSet
        (sourceSet.kind(),
            result.toArray(new org.nlogo.agent.Agent[result.size()]),
            world);
  }
}
