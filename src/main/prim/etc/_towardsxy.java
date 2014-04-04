// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;

public final strictfp class _towardsxy extends Reporter {

  @Override
  public Object report(Context context) {
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
