// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.I18N;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.RuntimePrimitiveException;
import org.nlogo.nvm.Reporter;

public final class _randomgamma extends Reporter {

  @Override
  public Object report(Context context) {
    return report_1(context,
        argEvalDoubleValue(context, 0),
        argEvalDoubleValue(context, 1));
  }

  public double report_1(Context context, double alpha, double lambda) {
    if (alpha <= 0 || lambda <= 0) {
      throw new RuntimePrimitiveException(context, this,
          I18N.errorsJ().getN("org.nlogo.prim.etc._randomgamma.noNegativeInputs", displayName()));
    }
    return validDouble(org.nlogo.agent.Gamma.nextDouble(context.job.random, alpha, lambda), context);
  }
}
