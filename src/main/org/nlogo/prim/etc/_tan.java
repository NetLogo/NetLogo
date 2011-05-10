package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;
import org.nlogo.nvm.Pure;

public final strictfp class _tan extends Reporter implements Pure {
  @Override
  public Syntax syntax() {
    int[] right = {Syntax.TYPE_NUMBER};
    int ret = Syntax.TYPE_NUMBER;
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
