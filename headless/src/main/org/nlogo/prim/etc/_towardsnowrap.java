// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.I18N;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;

public final strictfp class _towardsnowrap extends Reporter {
  @Override
  public Syntax syntax() {
    int[] right = {Syntax.TurtleType() | Syntax.PatchType()};
    return Syntax.reporterSyntax(right, Syntax.NumberType(), "-TP-");
  }

  @Override
  public Object report(Context context) {
    org.nlogo.agent.Agent agent = argEvalAgent(context, 0);
    if (agent instanceof org.nlogo.agent.Link) {
      throw new EngineException(context, this,
          I18N.errorsJ().get("org.nlogo.prim.etc.$common.expectedTurtleOrPatchButGotLink"));
    }
    if (agent.id() == -1) {
      throw new EngineException(context, this,
          I18N.errorsJ().getN("org.nlogo.$common.thatAgentIsDead", agent.classDisplayName()));
    }
    try {
      return validDouble(world.protractor().towards(context.agent, agent, false)); // false = don't wrap
    } catch (org.nlogo.api.AgentException ex) {
      throw new EngineException(context, this, ex.getMessage());
    }
  }
}
