// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.RuntimePrimitiveException;
import org.nlogo.nvm.Reporter;

public final class _towardsxynowrap extends Reporter {


  @Override
  public Object report(Context context) throws LogoException {
    try {
      return validDouble
          (world.protractor().towards
              (context.agent,
                  argEvalDoubleValue(context, 0),
                  argEvalDoubleValue(context, 1),
                  false), context); // true = don't wrap
    } catch (org.nlogo.api.AgentException ex) {
      throw new RuntimePrimitiveException
          (context, this, ex.getMessage());
    }
  }
}
