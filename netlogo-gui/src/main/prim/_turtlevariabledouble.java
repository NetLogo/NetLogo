// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.Turtle;
import org.nlogo.api.AgentException;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;

public final strictfp class _turtlevariabledouble extends Reporter {
  public int vn;

  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax(Syntax.NumberType(), "-T--");
  }

  @Override
  public String toString() {
    if (world == null) {
      return super.toString() + ":" + vn;
    }
    return super.toString() + ":" + world.turtlesOwnNameAt(vn);
  }

  @Override
  public Object report(Context context) throws LogoException {
    try {
      return context.agent.getTurtleVariable(vn);
    } catch (AgentException ex) {
      throw new EngineException(context, this, ex.getMessage());
    }
  }

  public double report_1(Context context) {
    return ((Turtle) context.agent).getTurtleVariableDouble(vn);
  }

  public Double report_2(Context context) throws LogoException {
    try {
      return (Double) context.agent.getTurtleVariable(vn);
    } catch (AgentException ex) {
      throw new EngineException(context, this, ex.getMessage());
    }
  }
}
