package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.api.Syntax;

public final strictfp class _setlinethickness
    extends Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.NumberType()},
            "-T--", true);
  }

  @Override
  public void perform(final Context context)
      throws LogoException {
    world.setLineThickness
        (context.agent,
            argEvalDoubleValue(context, 0));
    context.ip = next;
  }
}
