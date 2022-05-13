// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.Turtle;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;

public final class _jump
    extends Command {
  public _jump() {
    this.switches = true;
  }



  @Override
  public void perform(Context context) throws LogoException {
    perform_1(context, argEvalDoubleValue(context, 0));
  }

  public void perform_1(Context context, double distance) {
    try {
      ((Turtle) context.agent).jump(distance);
    } catch (org.nlogo.api.AgentException e) { } // NOPMD
    context.ip = next;
  }
}
