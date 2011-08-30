package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;

public final strictfp class _clearall
    extends org.nlogo.nvm.Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax("O---", true);
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    perform_1(context);
  }

  public void perform_1(final org.nlogo.nvm.Context context) throws LogoException {
    workspace.clearAll();
    context.ip = next;
  }
}
