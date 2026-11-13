// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.Agent;
import org.nlogo.core.LocaleContextJ;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.RuntimePrimitiveException;
import org.nlogo.nvm.Reporter;

public final class _towards extends Reporter {

  @Override
  public Object report(Context context) {
    Agent agent = argEvalAgent(context, 0);
    if (agent instanceof org.nlogo.agent.Link) {
      throw new RuntimePrimitiveException(context, this,
        LocaleContextJ.loc("Expected a turtle or a patch but got a link."));
    }
    if (agent.id() == -1) {
      throw new RuntimePrimitiveException(context, this,
        LocaleContextJ.loc("That {} is dead.", agent.classDisplayName()));
    }
    try {
      return validDouble
          (world.protractor().towards
              (context.agent, agent, true), context); // true = wrap
    } catch (org.nlogo.api.AgentException ex) {
      throw new RuntimePrimitiveException
          (context, this, ex.getMessage());
    }
  }
}
