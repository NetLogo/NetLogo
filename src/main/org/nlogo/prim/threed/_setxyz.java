package org.nlogo.prim.threed;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;

public final strictfp class _setxyz
    extends Command {
  @Override
  public Syntax syntax() {
    int[] right = {Syntax.TYPE_NUMBER,
        Syntax.TYPE_NUMBER,
        Syntax.TYPE_NUMBER};
    return Syntax.commandSyntax(right, "OT--", true);
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    if (context.agent instanceof org.nlogo.agent.Turtle) {
      org.nlogo.agent.Turtle3D turtle = (org.nlogo.agent.Turtle3D) context.agent;
      Double newx = argEvalDouble(context, 0);
      Double newy = argEvalDouble(context, 1);
      Double newz = argEvalDouble(context, 2);
      try {
        double xvalue = newx.doubleValue();
        double yvalue = newy.doubleValue();
        double zvalue = newz.doubleValue();

        double x = turtle.shortestPathX(xvalue);
        double y = turtle.shortestPathY(yvalue);
        double z = turtle.shortestPathZ(zvalue);
        if (x != xvalue) {
          newx = Double.valueOf(x);
        }
        if (y != yvalue) {
          newy = Double.valueOf(y);
        }
        if (z != zvalue) {
          newz = Double.valueOf(z);
        }
        turtle.xyandzcor(newx, newy, newz);
      } catch (org.nlogo.api.AgentException e) {
        throw new EngineException
            (context, this,
                "The point [ "
                    + newx.doubleValue() + " , "
                    + newy.doubleValue() + " , "
                    + newz.doubleValue() + " ] "
                    + "is outside of the boundaries of the world "
                    + "and wrapping is not permitted in one or both directions.");
      }

    } else {
      double xcor = argEvalDoubleValue(context, 0);
      double ycor = argEvalDoubleValue(context, 1);
      double zcor = argEvalDoubleValue(context, 2);
      org.nlogo.agent.Observer3D observer = (org.nlogo.agent.Observer3D) world.observer();
      org.nlogo.api.Vect rotationPoint = observer.rotationPoint();
      observer.oxyandzcor(xcor, ycor, zcor);
      observer.face(rotationPoint.x(), rotationPoint.y(), rotationPoint.z());
    }

    context.ip = next;
  }
}
