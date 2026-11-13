// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.LocaleContextJ;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.RuntimePrimitiveException;
import org.nlogo.core.Pure;
import org.nlogo.nvm.Reporter;

public final class _sqrt extends Reporter implements Pure {


  @Override
  public Object report(Context context) throws LogoException {
    return report_1(context, argEvalDoubleValue(context, 0));
  }

  public double report_1(Context context, double arg0) throws LogoException {
    if (arg0 < 0) {
      throw new RuntimePrimitiveException(context, this,
        LocaleContextJ.loc("The square root of {} is an imaginary number.", LocaleContextJ.formatDouble(arg0)));
    }
    return StrictMath.sqrt(arg0);
  }
}
