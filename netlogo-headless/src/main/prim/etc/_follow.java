// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.Turtle;
import org.nlogo.api.PerspectiveJ;
import org.nlogo.core.I18N;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;

public final strictfp class _follow
    extends Command {

  public _follow() {
    switches = true;
  }

  @Override
  public void perform(final Context context) {
    Turtle turtle = argEvalTurtle(context, 0);
    if (turtle.id() == -1) {
      throw new EngineException(context, this,
        I18N.errorsJ().getN("org.nlogo.$common.thatAgentIsDead", turtle.classDisplayName()));
    }
    world.observer().setPerspective(PerspectiveJ.FOLLOW(), turtle);
    context.ip = next;
  }
}
