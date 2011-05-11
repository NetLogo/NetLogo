package org.nlogo.prim;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Pure;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _minus extends Reporter implements Pure {
  @Override
  public Syntax syntax() {
    int left = Syntax.TYPE_NUMBER;
    int[] right = {Syntax.TYPE_NUMBER};
    int ret = Syntax.TYPE_NUMBER;
    return Syntax.reporterSyntax(left, right, ret,
        Syntax.NORMAL_PRECEDENCE - 3);
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
