package org.nlogo.prim.etc;

import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.api.Syntax;

public final strictfp class _display
    extends Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax(true);
  }

  @Override
  public void perform(final Context context) {
    world.displayOn(true);
    workspace.requestDisplayUpdate(true);
    context.ip = next;
  }
}
