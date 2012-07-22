// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Turtle;
import org.nlogo.api.AgentKindJ;
import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;

import java.util.List;

public final strictfp class _incone
    extends Reporter {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (Syntax.TurtlesetType() | Syntax.PatchsetType(),          // left
            new int[]{Syntax.NumberType(), Syntax.NumberType()}, // right
            Syntax.TurtlesetType() | Syntax.PatchsetType(),          // return
            org.nlogo.api.Syntax.NormalPrecedence() + 2, false, "OTPL", "-T--");
  }

  @Override
  public Object report(final Context context)
      throws LogoException {
    return report_1(context, argEvalAgentSet(context, 0),
        argEvalDoubleValue(context, 1),
        argEvalDoubleValue(context, 2));
  }

  public AgentSet report_1(final Context context, AgentSet sourceSet,
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
        world.inRadiusOrCone.inCone((Turtle) context.agent, sourceSet, radius, angle, true);
    return new org.nlogo.agent.ArrayAgentSet
        (sourceSet.kind(),
            result.toArray(new Agent[result.size()]),
            world);
  }
}
