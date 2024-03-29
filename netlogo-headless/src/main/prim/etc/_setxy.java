// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.Turtle;
import org.nlogo.core.I18N;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.RuntimePrimitiveException;

public final class _setxy
    extends Command {

  public _setxy() {
    switches = true;
  }

  @Override
  public void perform(final Context context) {
    Turtle turtle = (Turtle) context.agent;
    Double newx = argEvalDouble(context, 0);
    Double newy = argEvalDouble(context, 1);
    try {
      double xvalue = newx.doubleValue();
      double yvalue = newy.doubleValue();
      double x = turtle.shortestPathX(xvalue);
      double y = turtle.shortestPathY(yvalue);
      if (x != xvalue) {
        newx = Double.valueOf(x);
      }
      if (y != yvalue) {
        newy = Double.valueOf(y);
      }
      turtle.xandycor(newx, newy);
    } catch (org.nlogo.api.AgentException e) {
      throw new RuntimePrimitiveException(context, this,
          I18N.errorsJ().getN("org.nlogo.prim.etc._setxy.pointOutsideWorld",
              newx.doubleValue(), newy.doubleValue()));
    }
    context.ip = next;
  }

  public void perform_1(final Context context, double xvalue, double yvalue) {
    Turtle turtle = (Turtle) context.agent;
    try {
      turtle.xandycor(turtle.shortestPathX(xvalue),
          turtle.shortestPathY(yvalue));
    } catch (org.nlogo.api.AgentException e) {
      throw new RuntimePrimitiveException(context, this,
          I18N.errorsJ().getN("org.nlogo.prim.etc._setxy.pointOutsideWorld", xvalue, yvalue));
    }
    context.ip = next;
  }
}
