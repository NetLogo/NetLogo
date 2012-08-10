// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.Agent;
import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;

public final strictfp class _distance extends Reporter {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (new int[]{Syntax.TurtleType() | Syntax.PatchType()},
            Syntax.NumberType(), "-TP-");
  }

  @Override
  public Object report(Context context) throws LogoException {
    return report_1(context, argEvalAgent(context, 0));
  }

  public double report_1(Context context, Agent otherAgent) throws LogoException {
    if (otherAgent instanceof org.nlogo.agent.Link) {
      throw new EngineException
          (context, this, I18N.errorsJ().get("org.nlogo.prim.etc.$common.expectedTurtleOrPatchButGotLink"));
    }
    if (otherAgent.id == -1) {
      throw new EngineException(context, this,
        I18N.errorsJ().getN("org.nlogo.$common.thatAgentIsDead", otherAgent.classDisplayName()));
    }
    return world.protractor().distance(context.agent, otherAgent, true); // true = wrap
  }
}
