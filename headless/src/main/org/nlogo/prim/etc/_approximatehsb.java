// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Pure;
import org.nlogo.nvm.Reporter;

public final strictfp class _approximatehsb extends Reporter implements Pure {
  @Override
  public Syntax syntax() {
    int[] right = {Syntax.NumberType(), Syntax.NumberType(), Syntax.NumberType()};
    int ret = Syntax.NumberType();
    return Syntax.reporterSyntax(right, ret);
  }

  @Override
  public Object report(Context context) {
    return validDouble(report_1(context,
        argEvalDoubleValue(context, 0),
        argEvalDoubleValue(context, 1),
        argEvalDoubleValue(context, 2)));
  }

  public double report_1(Context context, double h, double s, double b) {
    return org.nlogo.api.Color.getClosestColorNumberByHSB
        ((float) h, (float) s, (float) b);
  }
}
