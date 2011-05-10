package org.nlogo.prim;

import org.nlogo.agent.Turtle;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Syntax;

public final strictfp class _fd1
    extends Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax("-T--", true);
  }

  @Override
  public void perform(Context context) {
    perform_1(context);
  }

  public void perform_1(Context context) {
    try {
      ((Turtle) context.agent).jump(1);
    } catch (org.nlogo.api.AgentException e) {
    } // NOPMD
    context.ip = next;
  }
}
