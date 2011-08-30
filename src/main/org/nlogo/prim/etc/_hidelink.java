package org.nlogo.prim.etc;

import org.nlogo.agent.Link;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.api.Syntax;

public final strictfp class _hidelink
    extends Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax("---L", true);
  }

  @Override
  public void perform(final Context context) {
    ((Link) context.agent).hidden(true);
    context.ip = next;
  }
}
