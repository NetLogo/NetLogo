package org.nlogo.prim.hubnet;

import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.nvm.Syntax;

public final strictfp class _hubnetsetclientinterface
    extends org.nlogo.nvm.Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.TYPE_STRING, Syntax.TYPE_LIST},
            "O---", false);
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    final String interfaceType = argEvalString(context, 0);
    final LogoList interfaceInfo = argEvalList(context, 1);
    workspace.waitFor
        (new org.nlogo.api.CommandRunnable() {
          public void run() throws LogoException {
            workspace.getHubNetManager().setClientInterface(interfaceType, interfaceInfo);
          }
        });
    context.ip = next;
  }
}
