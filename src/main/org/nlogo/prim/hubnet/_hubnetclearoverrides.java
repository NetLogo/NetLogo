package org.nlogo.prim.hubnet;

import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;

public strictfp class _hubnetclearoverrides
    extends org.nlogo.nvm.Command {
  int vn;

  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax(new int[]{Syntax.StringType()}, "OTPL", false);
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    final String client = argEvalString(context, 0);

    workspace.waitFor
        (new org.nlogo.api.CommandRunnable() {
          public void run() {
            workspace.getHubNetManager().clearOverrideLists(client);
          }
        });
    context.ip = next;
  }
}
