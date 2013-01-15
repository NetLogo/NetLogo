// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Pure;
import org.nlogo.nvm.Reporter;

public final strictfp class _scalecolor extends Reporter implements Pure {
  @Override
  public Syntax syntax() {
    int[] right = {Syntax.NumberType(), Syntax.NumberType(),
        Syntax.NumberType(), Syntax.NumberType()};
    return Syntax.reporterSyntax(right, Syntax.NumberType());
  }

  @Override
  public Object report(Context context) {
    return report_1(context,
        argEvalDoubleValue(context, 0),
        argEvalDoubleValue(context, 1),
        argEvalDoubleValue(context, 2),
        argEvalDoubleValue(context, 3));
  }

  public double report_1(Context context, double color, double var, double min, double max) {
    // shade is irrelevant (i.e. black, white, and grey do same thing)
    color = org.nlogo.api.Color.findCentralColorNumber(color) - 5.0;
    double perc = 0.0;
    if (min > max)      // min and max are really reversed
    {
      if (var < max) {
        perc = 1.0;
      } else if (var > min) {
        perc = 0.0;
      } else {
        double tempval = min - var;
        double tempmax = min - max;
        perc = tempval / tempmax;
      }
    } else {
      if (var > max) {
        perc = 1.0;
      } else if (var < min) {
        perc = 0.0;
      } else {
        double tempval = var - min;
        double tempmax = max - min;
        perc = tempval / tempmax;
      }
    }
    perc *= 10;
    if (perc >= 9.9999) {
      perc = 9.9999;
    } else if (perc < 0) {
      perc = 0;
    }
    return validDouble(color + perc);
  }
}
