package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Syntax;

public final strictfp class _magicopen
    extends Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.TYPE_STRING},
            "O---", true);
  }

  @Override
  public void perform(final Context context)
      throws LogoException {
    perform_1(context, argEvalString(context, 0));
  }

  public void perform_1(final Context context, String name) {
    workspace.magicOpen(name);
    context.ip = next;
  }
}
