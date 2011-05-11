package org.nlogo.prim.etc;

import org.nlogo.agent.Turtle;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Syntax;

public final strictfp class _left
    extends Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.TYPE_NUMBER},
            "-T--", true);
  }

  @Override
  public void perform(final Context context)
      throws LogoException {
    perform_1(context, argEvalDoubleValue(context, 0));
  }

  public void perform_1(final Context context, double delta) {
    ((Turtle) context.agent).turnRight(-delta);
    context.ip = next;
  }
}
