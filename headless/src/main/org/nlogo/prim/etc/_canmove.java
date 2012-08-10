// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final strictfp class _canmove
    extends Reporter {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (new int[]{Syntax.NumberType()},
            Syntax.BooleanType(),
            "-T--");
  }

  @Override
  public Object report(final Context context) throws LogoException {
    double distance = argEvalDoubleValue(context, 0);
    org.nlogo.agent.Turtle turtle = (org.nlogo.agent.Turtle) context.agent;
    try {
      world.protractor().getPatchAtHeadingAndDistance(turtle.xcor(), turtle.ycor(), turtle.heading(), distance);
    } catch (org.nlogo.api.AgentException exc) {
      return Boolean.FALSE;
    }
    return Boolean.TRUE;
  }
}
