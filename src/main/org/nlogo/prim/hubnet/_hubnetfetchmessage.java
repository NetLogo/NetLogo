package org.nlogo.prim.hubnet;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Syntax;

public final strictfp class _hubnetfetchmessage
    extends org.nlogo.nvm.Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax();
  }

  @Override
  public void perform(final Context context)
      throws LogoException {
    workspace.getHubNetManager().fetchMessage();
    context.ip = next;
  }
}
