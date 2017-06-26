// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.Agent;
import org.nlogo.core.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.RuntimePrimitiveException;
import org.nlogo.nvm.Reporter;

public final strictfp class _towards extends Reporter {


  @Override
  public Object report(Context context) throws LogoException {
    Agent agent = argEvalAgent(context, 0);
    if (agent instanceof org.nlogo.agent.Link) {
      throw new RuntimePrimitiveException
          (context, this, I18N.errorsJ().get("org.nlogo.prim.etc.$common.expectedTurtleOrPatchButGotLink"));
    }
    if (agent.id() == -1) {
      throw new RuntimePrimitiveException(context, this,
        I18N.errorsJ().getN("org.nlogo.$common.thatAgentIsDead", agent.classDisplayName()));
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
