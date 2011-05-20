package org.nlogo.prim.threed;

import org.nlogo.api.I18N;
import org.nlogo.api.I18NJava;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _towardspitch
    extends Reporter {
  @Override
  public Syntax syntax() {
    int[] right = {Syntax.TYPE_TURTLE | Syntax.TYPE_PATCH};
    int ret = Syntax.TYPE_NUMBER;
    return Syntax.reporterSyntax(right, ret, "-TP-");
  }

  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    org.nlogo.agent.Agent agent = argEvalAgent(context, 0);
    if (agent.id == -1) {
      throw new EngineException(context, this,
          I18NJava.errors().getN("org.nlogo.$common.thatAgentIsDead", agent.classDisplayName()));
    }
    try {
      return newValidDouble(world.protractor().towardsPitch(context.agent, agent, true)); // true = wrap
    } catch (org.nlogo.api.AgentException ex) {
      throw new EngineException(context, this, ex.getMessage());
    }
  }
}
