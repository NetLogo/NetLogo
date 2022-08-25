// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.core.Pure;
import org.nlogo.nvm.Reporter;

public final class _tan extends Reporter implements Pure {


  @Override
  public Object report(Context context) throws LogoException {
    return report_1(context, argEvalDoubleValue(context, 0));
  }

  public double report_1(Context context, double angle) throws LogoException {
    double mod = Math.abs(angle % 180);
    if (mod == 90) {
      validDouble(java.lang.Double.POSITIVE_INFINITY, context);
    }
    if (mod == 0) {
      return 0;
    } else {
      return StrictMath.tan(StrictMath.toRadians(angle));
    }
  }
}
