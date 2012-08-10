// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Pure;
import org.nlogo.nvm.Reporter;

public final strictfp class _minus extends Reporter implements Pure {
  @Override
  public Syntax syntax() {
    int left = Syntax.NumberType();
    int[] right = {Syntax.NumberType()};
    int ret = Syntax.NumberType();
    return Syntax.reporterSyntax(left, right, ret,
        org.nlogo.api.Syntax.NormalPrecedence() - 3);
  }

  @Override
  public Object report(Context context) throws LogoException {
    return report_1(context,
        argEvalDoubleValue(context, 0),
        argEvalDoubleValue(context, 1));
  }

  public double report_1(Context context, double arg0, double arg1) {
    return arg0 - arg1;
  }
}
