// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Pure;
import org.nlogo.nvm.Reporter;

public final strictfp class _atan extends Reporter implements Pure {
  @Override
  public org.nlogo.core.Syntax syntax() {
    int[] right = {Syntax.NumberType(), Syntax.NumberType()};
    return Syntax.reporterSyntax(right, Syntax.NumberType());
  }

  @Override
  public Object report(Context context) throws LogoException {
    return report_1(context,
        argEvalDoubleValue(context, 0),
        argEvalDoubleValue(context, 1));
  }

  public double report_1(Context context, double d1, double d2) throws LogoException {
    if (d1 == 0 && d2 == 0) {
      throw new EngineException(context, this, I18N.errorsJ().get("org.nlogo.prim.etc.atan.bothInputsCannotBeZero"));
    }
    if (d1 == 0) {
      return d2 > 0 ? 0 : 180;
    }
    if (d2 == 0) {
      return d1 > 0 ? 90 : 270;
    }
    return (StrictMath.toDegrees(StrictMath.atan2(d1, d2))
        + 360)
        % 360;
  }
}
