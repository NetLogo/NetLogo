// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Pure;
import org.nlogo.nvm.Reporter;

public final strictfp class _tan extends Reporter implements Pure {
  @Override
  public Syntax syntax() {
    int[] right = {Syntax.NumberType()};
    int ret = Syntax.NumberType();
    return Syntax.reporterSyntax(right, ret);
  }

  @Override
  public Object report(Context context) throws LogoException {
    return report_1(context, argEvalDoubleValue(context, 0));
  }

  public double report_1(Context context, double angle) throws LogoException {
    return validDouble(StrictMath.tan(StrictMath.toRadians(angle)));
  }
}
