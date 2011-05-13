package org.nlogo.prim.etc;

import java.util.ArrayList;
import java.util.List;

import org.nlogo.agent.Agent;
import org.nlogo.api.AgentException;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.ArrayAgentSet;
import org.nlogo.agent.Patch;
import org.nlogo.agent.Turtle;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _inrect
    extends Reporter {
  @Override
  public Syntax syntax() {
    int left = Syntax.TYPE_AGENTSET;
    int[] right = {Syntax.TYPE_NUMBER,
        Syntax.TYPE_NUMBER};
    int ret = Syntax.TYPE_AGENTSET;
    return Syntax.reporterSyntax
        (left, right, ret, Syntax.NORMAL_PRECEDENCE + 2,
            false, "-TP-", null);
  }

  @Override
  public Object report(final org.nlogo.nvm.Context context) throws LogoException {
    // There seems to be a bug in this code, if you
    // try large distX and distY, it still doesn't cover the
    // whole world.  ~Forrest (4/14/2007)
    AgentSet sourceSet = argEvalAgentSet(context, 0);
    List<Agent> result = new ArrayList<Agent>();
    double distX = argEvalDoubleValue(context, 1);
    double distY = argEvalDoubleValue(context, 2);

    Turtle startTurtle = null;
    Patch startPatch;
    if (context.agent instanceof Turtle) {
      startTurtle = (Turtle) context.agent;
      startPatch = startTurtle.getPatchHere();
    } else {
      startPatch = (Patch) context.agent;
    }

    int dxmin = 0;
    int dxmax = 0;
    int dymin = 0;
    int dymax = 0;

    if (world.wrappingAllowedInX()) {
      dxmax = (int) StrictMath.ceil(StrictMath.min(distX, world.worldWidth() / 2));
      dxmin = -(dxmax);
    } else {
      int xdiff = world.minPxcor() - startPatch.pxcor;
      dxmin = (int) StrictMath.ceil(StrictMath.abs(xdiff) < distX ? xdiff : -distX);
      dxmax = (int) StrictMath.ceil
          (StrictMath.min((world.maxPxcor() - startPatch.pxcor), distX));
    }
    if (world.wrappingAllowedInY()) {
      dymax = (int) StrictMath.ceil
          (StrictMath.min(distY, (int) StrictMath.ceil(world.worldHeight() / 2)));
      dymin = -(dymax);
    } else {
      int ydiff = world.minPycor() - startPatch.pycor;
      dymin = (int) StrictMath.ceil(StrictMath.abs(ydiff) < distY ? ydiff : -distY);
      dymax = (int) StrictMath.ceil
          (StrictMath.min((world.maxPycor() - startPatch.pycor), distY));
    }

    for (int dy = dymin; dy <= dymax; dy++) {
      for (int dx = dxmin; dx <= dxmax; dx++) {
        Patch patch = null;
        try {
          patch = startPatch.getPatchAtOffsets(dx, dy);
        } catch (AgentException e) { } // NOPMD
        if (patch != null) {
          if (sourceSet.type() == Patch.class) {
            if (startTurtle == null) {
              if (sourceSet == world.patches() || sourceSet.contains(patch)) {
                result.add(patch);
              }
            } else {
              if (world.protractor().distance(patch.pxcor, 0, startTurtle.xcor(), 0, true) <= distX &&
                  world.protractor().distance(0, patch.pycor, 0, startTurtle.ycor(), true) <= distY &&
                  (sourceSet == world.patches() || sourceSet.contains(patch))) {
                result.add(patch);
              }
            }
          } else {
            for (Turtle turtle : patch.turtlesHere()) {
              if (sourceSet == world.turtles() || sourceSet.contains(turtle)) {
                if (startTurtle == null) {
                  if ((world.protractor().distance(turtle.xcor(), 0, startPatch.pxcor, 0, true) <= distX) &&
                      (world.protractor().distance(0, turtle.ycor(), 0, startPatch.pycor, true) <= distY)) {
                    result.add(turtle);
                  }
                } else {
                  if ((world.protractor().distance(turtle.xcor(), 0, startTurtle.xcor(), 0, true) <= distX) &&
                      (world.protractor().distance(0, turtle.ycor(), 0, startTurtle.ycor(), true) <= distY)) {
                    result.add(turtle);
                  }
                }
              }
            }
          }
        }
      }
    }
    return new ArrayAgentSet
        (sourceSet.type(), result.toArray(new Agent[result.size()]), world);
  }
}
