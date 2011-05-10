package org.nlogo.prim.etc;

import org.nlogo.nvm.Command;
import org.nlogo.nvm.Syntax;
import org.nlogo.api.Perspective;

public final strictfp class _watchme
    extends Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax("-TPL", true);
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context) {
    world.observer().home();
    world.observer().setPerspective(Perspective.WATCH, context.agent);
    context.ip = next;
  }
}
