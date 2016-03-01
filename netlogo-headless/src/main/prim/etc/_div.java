// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.I18N;
import org.nlogo.core.Pure;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;

public final strictfp class _div extends Reporter implements Pure {
  @Override
  public Object report(Context context) {
    return report_1(context,
        argEvalDoubleValue(context, 0),
        argEvalDoubleValue(context, 1));
  }

  public double report_1(Context context, double arg0, double arg1) {
    if (arg1 == 0) {
      throw new EngineException(context, this, I18N.errorsJ().get("org.nlogo.prim.etc.$common.divByZero"));
    }
    return arg0 / arg1;
  }
}
