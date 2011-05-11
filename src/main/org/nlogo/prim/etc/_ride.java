package org.nlogo.prim.etc;

import org.nlogo.agent.Turtle;
import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;
import org.nlogo.api.Perspective;

public final strictfp class _ride
    extends Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.TYPE_TURTLE},
            "O---", true);
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    Turtle turtle = argEvalTurtle(context, 0);
    if (turtle.id == -1) {
      throw new EngineException(context, this,
        I18N.errors().getNJava("org.nlogo.$common.thatAgentIsDead", new String[]{turtle.classDisplayName()}));
    }
    world.observer().setPerspective(Perspective.RIDE, turtle);
    world.observer().followDistance(0);
    context.ip = next;
  }
}
