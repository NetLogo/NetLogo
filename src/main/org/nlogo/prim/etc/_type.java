package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Syntax;

public final strictfp class _type
    extends org.nlogo.nvm.Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.TYPE_WILDCARD});
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    workspace.outputObject
        (args[0].report(context), null, false, false,
            org.nlogo.nvm.Workspace.OutputDestination.NORMAL);
    context.ip = next;
  }
}
