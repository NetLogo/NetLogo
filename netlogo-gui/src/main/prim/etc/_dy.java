// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.Turtle;
import org.nlogo.api.Constants;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

public final strictfp class _dy extends Reporter {
  @Override
  public org.nlogo.core.Syntax syntax() {
    return Syntax.reporterSyntax
        (Syntax.NumberType(), "-T--");
  }

  @Override
  public Object report(Context context) {
    return report_1(context);
  }

  public double report_1(Context context) {
    double value = ((Turtle) context.agent).dy();
    return (StrictMath.abs(value) < Constants.Infinitesimal())
        ? 0 : value;
  }
}
