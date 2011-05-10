package org.nlogo.prim.gui;

import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;

public final strictfp class _inspect
    extends org.nlogo.nvm.Command {
  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    final org.nlogo.agent.Agent agent = argEvalAgent(context, 0);
    if (agent.id == -1) {
      throw new EngineException
          (context, this, I18N.errors().get("org.nlogo.$common.thatTurtleIsDead"));

    }
    org.nlogo.awt.Utils.invokeLater
        (new Runnable() {
          public void run() {
            // we usually use a default radius of 3, but that doesnt work when the world
            // has a radius of less than 3. so simply take the miniumum. - JC 7/1/10
            double minWidthOrHeight =
                StrictMath.min(workspace.world().worldWidth() / 2, workspace.world().worldHeight() / 2);
            double radius = StrictMath.min(3, minWidthOrHeight / 2);
            workspace.inspectAgent(agent.getAgentClass(), agent, radius);
          }
        });
    context.ip = next;
  }

  @Override
  public Syntax syntax() {
    int[] right = {Syntax.TYPE_AGENT};
    return Syntax.commandSyntax(right);
  }
}
