package org.nlogo.prim.hubnet;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Syntax;

public final strictfp class _hubnetbroadcastclearoutput
    extends org.nlogo.nvm.Command {
  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    workspace.getHubNetManager().broadcastClearText();
    context.ip = next;
  }

  @Override
  public Syntax syntax() {
    int[] right = {};
    return Syntax.commandSyntax(right);
  }
}
