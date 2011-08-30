package org.nlogo.prim.etc;

import org.nlogo.api.Syntax;

public final strictfp class _resettimer
    extends org.nlogo.nvm.Command {
  @Override
  public void perform(final org.nlogo.nvm.Context context) {
    perform_1(context);
  }

  @Override
  public Syntax syntax() {
    int[] right = {};
    return Syntax.commandSyntax(right);
  }

  public void perform_1(final org.nlogo.nvm.Context context) {
    workspace.world().timer.reset();
    context.ip = next;
  }
}
