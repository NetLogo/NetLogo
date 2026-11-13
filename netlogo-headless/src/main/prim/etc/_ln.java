// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.LocaleContextJ;
import org.nlogo.core.Pure;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.RuntimePrimitiveException;
import org.nlogo.nvm.Reporter;

public final class _ln extends Reporter implements Pure {

  @Override
  public Object report(Context context) {
    return report_1(context, argEvalDoubleValue(context, 0));
  }

  public double report_1(Context context, double d) {
    if (d <= 0) {
      throw new RuntimePrimitiveException(context, this,
        LocaleContextJ.loc("Can't take logarithm of {}.", LocaleContextJ.formatDouble(d)));
    }
    return validDouble(StrictMath.log(d), context);
  }
}
