package org.nlogo.prim.etc;

import org.nlogo.nvm.Command;
import org.nlogo.nvm.Syntax;

public final strictfp class _stamp
    extends Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax("-T-L", true);
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context) {
    world.stamp(context.agent, false);
    context.ip = next;
  }
}
