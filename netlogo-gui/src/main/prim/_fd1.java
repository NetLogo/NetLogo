// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.Turtle;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;

public final class _fd1
    extends Command {
  public _fd1() {
    this.switches = true;
  }



  @Override
  public void perform(Context context) {
    perform_1(context);
  }

  public void perform_1(Context context) {
    try {
      ((Turtle) context.agent).jump(1);
    } catch (org.nlogo.api.AgentException e) { } // NOPMD
    context.ip = next;
  }
}
