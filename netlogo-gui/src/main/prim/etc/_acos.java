// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.core.Pure;
import org.nlogo.nvm.Reporter;

public final strictfp class _acos extends Reporter implements Pure {


  @Override
  public Object report(Context context) throws LogoException {
    return report_1(context, argEvalDoubleValue(context, 0));
  }

  public double report_1(Context context, double arg0) throws LogoException {
    return validDouble(StrictMath.toDegrees(StrictMath.acos(arg0)), context);
  }
}
