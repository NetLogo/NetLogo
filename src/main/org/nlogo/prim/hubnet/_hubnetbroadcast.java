package org.nlogo.prim.hubnet;

import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;

public final strictfp class _hubnetbroadcast
    extends org.nlogo.nvm.Command {
  @Override
  public Syntax syntax() {
    int[] right = {Syntax.StringType(),
        Syntax.WildcardType()};
    return Syntax.commandSyntax(right);
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    String variableName = argEvalString(context, 0);
    Object data = args[1].report(context);
    workspace.getHubNetManager().broadcast(variableName, data);
    context.ip = next;
  }
}
