package org.nlogo.prim.etc;

import org.nlogo.agent.Layouts;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Syntax;

public final strictfp class _layoutquick
    extends Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax(true);
  }

  @Override
  public void perform(final Context context) {
    Layouts.spring
        (world.turtles(), world.links(),
            0.2, world.worldWidth() / 5, 0.2,
            context.job.random);
    context.ip = next;
  }
}
