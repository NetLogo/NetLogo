// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.api.PerspectiveJ;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.EngineException;

public final strictfp class _watch
    extends Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.AgentType()},
            "O---", true);
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    org.nlogo.agent.Agent agent = argEvalAgent(context, 0);
    if (agent.id == -1) {
      throw new EngineException(context, this,
        I18N.errorsJ().getN("org.nlogo.$common.thatAgentIsDead", agent.classDisplayName()));
    }
    world.observer().home();
    world.observer().setPerspective(PerspectiveJ.WATCH(), agent);
    context.ip = next;
  }
}
