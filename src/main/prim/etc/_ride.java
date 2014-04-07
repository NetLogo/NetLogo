// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.Turtle;
import org.nlogo.api.I18N;
import org.nlogo.api.PerspectiveJ;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.EngineException;

public final strictfp class _ride
    extends Command {
  @Override
  public Syntax syntax() {
    return SyntaxJ.commandSyntax
        (new int[]{Syntax.TurtleType()},
            "O---", true);
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context) {
    Turtle turtle = argEvalTurtle(context, 0);
    if (turtle.id() == -1) {
      throw new EngineException(context, this,
        I18N.errorsJ().getN("org.nlogo.$common.thatAgentIsDead", turtle.classDisplayName()));
    }
    world.observer().setPerspective(PerspectiveJ.RIDE(), turtle);
    context.ip = next;
  }
}
