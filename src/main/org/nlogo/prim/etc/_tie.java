package org.nlogo.prim.etc;

import org.nlogo.agent.Link;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Syntax;

public final strictfp class _tie
    extends Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax("---L", true);
  }

  @Override
  public void perform(final Context context) {
    ((Link) context.agent).mode(Link.MODE_FIXED);
    context.ip = next;
  }
}
