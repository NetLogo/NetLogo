package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.api.Syntax;

public final strictfp class _randomseed
    extends Command {
  @Override
  public void perform(final Context context) throws LogoException {
    perform_1(context, argEvalDoubleValue(context, 0));
  }

  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.NumberType()});
  }

  ////////////////
  // for new compiler

  public void perform_1(final Context context, double arg0) throws LogoException {
    context.job.random.setSeed(validLong(arg0));
    context.ip = next;
  }
}
