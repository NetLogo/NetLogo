// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.Agent;
import org.nlogo.core.LocaleContextJ;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.RuntimePrimitiveException;
import org.nlogo.nvm.Reporter;

public final class _distance extends Reporter {


  @Override
  public Object report(Context context) throws LogoException {
    return report_1(context, argEvalAgent(context, 0));
  }

  public double report_1(Context context, Agent otherAgent) throws LogoException {
    if (otherAgent instanceof org.nlogo.agent.Link) {
      throw new RuntimePrimitiveException(context, this,
        LocaleContextJ.loc("Expected a turtle or a patch but got a link."));
    }
    if (otherAgent.id() == -1) {
      throw new RuntimePrimitiveException(context, this,
        LocaleContextJ.loc("That {} is dead.", otherAgent.classDisplayName()));
    }
    return world.protractor().distance(context.agent, otherAgent, true); // true = wrap
  }
}
