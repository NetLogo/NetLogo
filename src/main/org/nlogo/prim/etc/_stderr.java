package org.nlogo.prim.etc;

import org.nlogo.api.Dump;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.api.Syntax;

public final strictfp class _stderr
    extends Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.WildcardType()});
  }

  @Override
  public void perform(final Context context)
      throws LogoException {
    System.err.println
        (Dump.logoObject(args[0].report(context)));
    context.ip = next;
  }
}
