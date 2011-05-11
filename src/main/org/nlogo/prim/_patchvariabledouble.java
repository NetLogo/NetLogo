package org.nlogo.prim;

import org.nlogo.api.AgentException;
import org.nlogo.api.LogoException;
import org.nlogo.agent.Patch;
import org.nlogo.agent.Turtle;
import org.nlogo.nvm.Syntax;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;

public final strictfp class _patchvariabledouble extends Reporter {
  public int vn;

  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax(Syntax.TYPE_NUMBER, "-TP-");
  }

  @Override
  public String toString() {
    if (world == null) {
      return super.toString() + ":" + vn;
    }
    return super.toString() + ":" + world.patchesOwnNameAt(vn);
  }

  @Override
  public Object report(Context context) throws LogoException {
    return report_1(context);
  }

  public Double report_1(Context context) throws LogoException {
    try {
      return (Double) context.agent.getPatchVariable(vn);
    } catch (AgentException ex) {
      throw new EngineException(context, this, ex.getMessage());
    }
  }

  public double report_2(Context context) {
    Patch patch =
        (context.agent instanceof Turtle)
            ? ((Turtle) context.agent).getPatchHere()
            : (Patch) context.agent;
    return patch.getPatchVariableDouble(vn);
  }

}
