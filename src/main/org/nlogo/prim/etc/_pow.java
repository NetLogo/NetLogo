// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Pure;
import org.nlogo.nvm.Reporter;

public final strictfp class _pow extends Reporter implements Pure {
  @Override
  public Syntax syntax() {
    int left = Syntax.NumberType();
    int[] right = {Syntax.NumberType()};
    return Syntax.reporterSyntax(left, right, Syntax.NumberType(),
        org.nlogo.api.Syntax.NormalPrecedence() - 1);
  }

  @Override
  public Object report(Context context) {
    return report_1(context,
        argEvalDoubleValue(context, 0),
        argEvalDoubleValue(context, 1));
  }

  public double report_1(Context context, double d0, double d1) {
    return validDouble(StrictMath.pow(d0, d1));
  }
}
