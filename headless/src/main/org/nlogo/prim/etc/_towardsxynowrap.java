// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;

public final strictfp class _towardsxynowrap extends Reporter {
  @Override
  public Syntax syntax() {
    int[] right = {Syntax.NumberType(), Syntax.NumberType()};
    return Syntax.reporterSyntax
        (right, Syntax.NumberType(), "-TP-");
  }

  @Override
  public Object report(Context context) throws LogoException {
    try {
      return validDouble
          (world.protractor().towards
              (context.agent,
                  argEvalDoubleValue(context, 0),
                  argEvalDoubleValue(context, 1),
                  false)); // true = don't wrap
    } catch (org.nlogo.api.AgentException ex) {
      throw new EngineException
          (context, this, ex.getMessage());
    }
  }
}
