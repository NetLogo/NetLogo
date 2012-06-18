// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Pure;
import org.nlogo.nvm.Reporter;

public final strictfp class _plus extends Reporter implements Pure {
  @Override
  public Syntax syntax() {
    int left = Syntax.NumberType();
    int[] right = {Syntax.NumberType()};
    return Syntax.reporterSyntax(left, right, Syntax.NumberType(),
        org.nlogo.api.Syntax.NormalPrecedence() - 3);
  }

  @Override
  public Object report(Context context) throws LogoException {
    return validDouble
        (argEvalDoubleValue(context, 0) +
            argEvalDoubleValue(context, 1));
  }

  public double report_1(Context context, double d1, double d2) {
    return d1 + d2;
  }
}
