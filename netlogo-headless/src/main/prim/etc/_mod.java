// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.LocaleContextJ;
import org.nlogo.core.Pure;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.RuntimePrimitiveException;
import org.nlogo.nvm.Reporter;

public final class _mod extends Reporter implements Pure {

  @Override
  public Object report(Context context) {
    return report_1(context,
        argEvalDoubleValue(context, 0),
        argEvalDoubleValue(context, 1));
  }

  public double report_1(Context context, double d0, double d1) {
    if (d1 == 0) {
      throw new RuntimePrimitiveException(context, this, LocaleContextJ.loc("Division by zero."));
    }
    return validDouble(d0 - (StrictMath.floor(d0 / d1) * d1), context);
  }
}
