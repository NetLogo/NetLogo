package org.nlogo.prim.gui;

import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;

public final strictfp class _inspectwithradius
    extends org.nlogo.nvm.Command {
  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    final org.nlogo.agent.Agent agent = argEvalAgent(context, 0);
    final double radius = argEvalDouble(context, 1);
    if (agent.id == -1) {
      throw new EngineException(context, this,
        I18N.errors().getNJava("org.nlogo.$common.thatAgentIsDead", new String[]{agent.classDisplayName()}));
    }
    if (radius < 0 || radius > ((world.worldWidth() - 1) / 2)) {
      throw new EngineException
          (context, this, "the radius must be between 0 and " + ((world.worldWidth() - 1) / 2));
    }

    org.nlogo.awt.Utils.invokeLater
        (new Runnable() {
          public void run() {
            workspace.inspectAgent(agent.getAgentClass(), agent, radius);
          }
        });

    context.ip = next;
  }

  @Override
  public Syntax syntax() {
    int[] right = {Syntax.TYPE_AGENT, Syntax.TYPE_NUMBER};
    return Syntax.commandSyntax(right);
  }
}
