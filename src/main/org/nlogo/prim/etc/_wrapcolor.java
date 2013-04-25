// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Pure;
import org.nlogo.nvm.Reporter;

public final strictfp class _wrapcolor extends Reporter implements Pure {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (new int[]{Syntax.NumberType()},
            Syntax.NumberType());
  }

  @Override
  public Object report(Context context) {
    return report_1(context, argEvalDoubleValue(context, 0));
  }

  public double report_1(Context context, double colorValue) {
    if (colorValue < 0 || colorValue >= 140) {
      return org.nlogo.api.Color.modulateDouble(colorValue);
    } else {
      return colorValue;
    }
  }
}
