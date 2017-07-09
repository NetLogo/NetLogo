// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.core.Pure;
import org.nlogo.nvm.Reporter;

public final strictfp class _abs extends Reporter implements Pure {


  @Override
  public Object report(Context context) throws LogoException {
    return report_1(context, argEvalDoubleValue(context, 0));
  }

  public Double report_1(Context context, Double d) {
    double unwrapped = d.doubleValue();
    return (unwrapped < 0) ? -unwrapped : d;
  }

  public double report_2(Context context, double d) {
    return (d < 0) ? -d : d;
  }
}
