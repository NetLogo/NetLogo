// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Pure;
import org.nlogo.nvm.Reporter;

public final strictfp class _sqrt extends Reporter implements Pure {
  @Override
  public Syntax syntax() {
    int[] right = {Syntax.NumberType()};
    return Syntax.reporterSyntax(right, Syntax.NumberType());
  }

  @Override
  public Object report(Context context) throws LogoException {
    return report_1(context, argEvalDoubleValue(context, 0));
  }

  public double report_1(Context context, double arg0) throws LogoException {
    if (arg0 < 0) {
      throw new EngineException(context, this,
          I18N.errorsJ().getN("org.nlogo.prim.etc._sqrt.squareRootIsImaginary", arg0));
    }
    return StrictMath.sqrt(arg0);
  }
}
