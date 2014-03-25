// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Pure;
import org.nlogo.nvm.Reporter;

public final strictfp class _approximatergb extends Reporter implements Pure {
  @Override
  public Syntax syntax() {
    int[] right = {Syntax.NumberType(), Syntax.NumberType(), Syntax.NumberType()};
    return Syntax.reporterSyntax(right, Syntax.NumberType());
  }

  @Override
  public Object report(Context context) {
    return report_1(context,
        argEvalDoubleValue(context, 0),
        argEvalDoubleValue(context, 1),
        argEvalDoubleValue(context, 2));
  }

  public double report_1(Context context, double rd, double gd, double bd) {
    float r = (float) rd;
    float g = (float) gd;
    float b = (float) bd;
    // restrict to 0-255 range
    r = (float) StrictMath.max(0.0, StrictMath.min(255, r));
    g = (float) StrictMath.max(0.0, StrictMath.min(255, g));
    b = (float) StrictMath.max(0.0, StrictMath.min(255, b));
    return validDouble
        (org.nlogo.api.Color.getClosestColorNumberByARGB
            ((0xff << 24) +
                (StrictMath.round(r) << 16) +
                (StrictMath.round(g) << 8) +
                (StrictMath.round(b))));
  }
}
