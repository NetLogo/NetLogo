package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;

public final strictfp class _towardsxy extends Reporter {
  @Override
  public Syntax syntax() {
    int[] right = {Syntax.TYPE_NUMBER,
        Syntax.TYPE_NUMBER};
    int ret = Syntax.TYPE_NUMBER;
    return Syntax.reporterSyntax(right, ret, "-TP-");
  }

  @Override
  public Object report(Context context) throws LogoException {
    try {
      return validDouble
          (world.protractor().towards
              (context.agent,
                  argEvalDoubleValue(context, 0),
                  argEvalDoubleValue(context, 1),
                  true)); // true = wrap
    } catch (org.nlogo.api.AgentException ex) {
      throw new EngineException
          (context, this, ex.getMessage());
    }
  }
}
