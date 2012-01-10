// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.Turtle;
import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.api.PerspectiveJ;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;

public final strictfp class _follow
    extends Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.TurtleType()},
            "O---", true);
  }

  @Override
  public void perform(final Context context)
      throws LogoException {
    Turtle turtle = argEvalTurtle(context, 0);
    if (turtle.id == -1) {
      throw new EngineException(context, this,
        I18N.errorsJ().getN("org.nlogo.$common.thatAgentIsDead", turtle.classDisplayName()));
    }
    world.observer().setPerspective(PerspectiveJ.FOLLOW(), turtle);
    // the following code is duplicated in _follow and _followme - ST 6/28/05
    int distance = (int) turtle.size() * 5;
    world.observer()
        .followDistance
            (StrictMath.max(1, StrictMath.min(distance, 100)));
    context.ip = next;
  }
}
