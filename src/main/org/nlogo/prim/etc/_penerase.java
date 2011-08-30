package org.nlogo.prim.etc;

import org.nlogo.agent.Turtle;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.api.Syntax;

public final strictfp class _penerase
    extends Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax("-T--", true);
  }

  @Override
  public void perform(final Context context) {
    ((Turtle) context.agent).penMode(Turtle.PEN_ERASE);
    context.ip = next;
  }
}
