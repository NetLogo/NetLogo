// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.Turtle;
import org.nlogo.api.AgentException;
import org.nlogo.api.Let;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.MutableDouble;

// note that this and _bkinternal are pretty much carbon copies of each other

public final strictfp class _fdinternal
    extends Command {
  private final Let let;

  public _fdinternal(_fd original) {
    let = original.let();
  }

  public _fdinternal(_bk original) {
    let = original.let();
  }

  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax("-T--", true);
  }

  @Override
  public void perform(Context context) {
    perform_1(context);
  }

  public void perform_1(Context context) {
    Turtle turtle = (Turtle) context.agent;
    MutableDouble countdown = (MutableDouble) context.getLet(let);
    double distance = countdown.value();
    double distanceMagnitude = StrictMath.abs(distance);
    if (distanceMagnitude <= org.nlogo.api.Constants.Infinitesimal()) {
      context.ip = next;
      return;
    }
    if (distanceMagnitude <= 1.0) {
      try {
        turtle.jump(distance);
      } catch (AgentException e) { } // NOPMD
      context.ip = next;
    } else {
      int stepDistance = (distance > 0) ? 1 : -1;
      try {
        turtle.jump(stepDistance);
        countdown.value_$eq(countdown.value() - stepDistance);
      } catch (AgentException e) {
        context.ip = next;
      }
    }
  }
}
