package org.nlogo.prim.hubnet;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Syntax;

public final strictfp class _hubnetroboclient
    extends org.nlogo.nvm.Command {
  @Override
  public Syntax syntax() {
    int[] right = {Syntax.TYPE_NUMBER};
    return Syntax.commandSyntax(right, "O---", false);
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    workspace.getHubNetManager().newClient(true, argEvalIntValue(context, 0));
    context.ip = next;
  }
}
