package org.nlogo.prim.etc;

import org.nlogo.agent.Link;
import org.nlogo.nvm.Command;
import org.nlogo.api.Syntax;

public final strictfp class _showlink
    extends Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax("---L", true);
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context) {
    ((Link) context.agent).hidden(false);
    context.ip = next;
  }
}
