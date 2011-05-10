package org.nlogo.prim.etc;

import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Syntax;

public final strictfp class _resetperspective
    extends Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax("OTPL", true);
  }

  @Override
  public void perform(final Context context) {
    world.observer().resetPerspective();
    context.ip = next;
  }
}
